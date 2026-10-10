package com.github.jewishbanana.ultimatecontent.build;

import static org.junit.Assert.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;

public class MobChipClassVersionNormalizerTest {
    private static final String ROOT = "me/gamercoder215/mobchip/";
    private static final String[] REQUIRED = {"EntityBrain", "bukkit/BukkitBrain", "abstraction/ChipUtilFactory",
        "abstraction/v1_17_R1/ChipUtil1_17_R1", "abstraction/v1_21_R3/ChipUtil1_21_R3",
        "abstraction/v26_1/ChipUtil26_1", "abstraction/v26_2/ChipUtil26_2", "abstraction/v26_3/ChipUtil26_3"};

    @Rule public TemporaryFolder temporary = new TemporaryFolder();

    private Path fixture(int version, int minor, boolean missingAdapter, boolean unrelatedNewClass) throws IOException {
        Path path = temporary.newFile("UltimateContent-test.jar").toPath();
        try (JarOutputStream jar = new JarOutputStream(Files.newOutputStream(path))) {
            for (String name : REQUIRED) {
                if (missingAdapter && name.contains("v26_2")) continue;
                byte[] bytes = bytecode(ROOT + name);
                if (name.contains("v26_3") || name.contains("v26_1")) {
                    bytes[4] = (byte) (minor >> 8);
                    bytes[5] = (byte) minor;
                    bytes[6] = (byte) (version >> 8);
                    bytes[7] = (byte) version;
                }
                put(jar, ROOT + name + ".class", bytes);
            }
            if (unrelatedNewClass) {
                byte[] bytes = bytecode("example/Unreviewed");
                bytes[7] = 69;
                put(jar, "example/Unreviewed.class", bytes);
            }
            put(jar, "plugin.yml", "name: UltimateContent\n".getBytes(java.nio.charset.StandardCharsets.UTF_8));
        }
        return path;
    }

    private static byte[] bytecode(String name) {
        ClassWriter writer = new ClassWriter(0);
        writer.visit(Opcodes.V17, Opcodes.ACC_PUBLIC, name, null, "java/lang/Object", null);
        writer.visitEnd();
        return writer.toByteArray();
    }

    private static void put(JarOutputStream jar, String name, byte[] bytes) throws IOException {
        jar.putNextEntry(new JarEntry(name));
        jar.write(bytes);
        jar.closeEntry();
    }

    @Test public void normalizesReviewedAdaptersAndPreservesJava17AndResources() throws Exception {
        Path path = fixture(69, 0, false, false);
        MobChipClassVersionNormalizer.main(new String[] {path.toString()});
        try (JarFile jar = new JarFile(path.toFile())) {
            for (String name : REQUIRED) {
                byte[] bytes = jar.getInputStream(jar.getJarEntry(ROOT + name + ".class")).readAllBytes();
                // Use precisely the ASM version used by Paper 1.21.4's remapper.
                new ClassReader(bytes).getClassName();
                assertEquals(name.contains("v26_3") || name.contains("v26_1") ? 65 : 61, bytes[7]);
            }
            assertEquals("name: UltimateContent\n", new String(jar.getInputStream(jar.getJarEntry("plugin.yml")).readAllBytes(), java.nio.charset.StandardCharsets.UTF_8));
        }
        byte[] once = Files.readAllBytes(path);
        MobChipClassVersionNormalizer.main(new String[] {path.toString()});
        assertArrayEquals(once, Files.readAllBytes(path));
    }

    @Test public void rejectsFutureBytecodeWithoutReplacingOriginal() throws Exception {
        Path path = fixture(70, 0, false, false);
        byte[] original = Files.readAllBytes(path);
        assertThrows(IOException.class, () -> MobChipClassVersionNormalizer.main(new String[] {path.toString()}));
        assertArrayEquals(original, Files.readAllBytes(path));
    }

    @Test public void rejectsPreviewBytecode() throws Exception {
        Path path = fixture(69, 65535, false, false);
        assertThrows(IOException.class, () -> MobChipClassVersionNormalizer.main(new String[] {path.toString()}));
    }

    @Test public void requiresBundledAdapters() throws Exception {
        Path path = fixture(65, 0, true, false);
        assertThrows(IOException.class, () -> MobChipClassVersionNormalizer.main(new String[] {path.toString()}));
    }

    @Test public void rejectsUnreviewedNewBytecodeRatherThanChangingIt() throws Exception {
        Path path = fixture(65, 0, false, true);
        assertThrows(IOException.class, () -> MobChipClassVersionNormalizer.main(new String[] {path.toString()}));
    }
}
