package com.github.jewishbanana.ultimatecontent.build;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.function.Consumer;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;
import java.util.jar.JarFile;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;

/**
 * Makes the Minecraft 26 MobChip implementation readable by older Paper remappers.
 *
 * MobChip's 26.x module is compiled on Java 25 because that Minecraft server requires Java 25. The module itself uses
 * ordinary bytecode, but its class-file header causes Paper 1.21.4's ASM version to reject the entire shaded plugin before
 * Bukkit can select the implementation for the running server. Lowering only that unused-on-older-servers module to the
 * Java 21 class-file version keeps it loadable on Java 25+ and parseable by every remapping Paper version we support.
 */
public final class MobChipClassVersionNormalizer {

	// Only adapters reviewed for the pinned upstream revision may be rewritten.
	private static final Set<String> NORMALIZABLE_PACKAGES = Set.of(
			"me/gamercoder215/mobchip/abstraction/v26_1/",
			"me/gamercoder215/mobchip/abstraction/v26_3/");
	private static final int JAVA_21_CLASS_VERSION = 65;
	private static final int JAVA_25_CLASS_VERSION = 69;

	private MobChipClassVersionNormalizer() {
	}

	public static void main(String[] args) throws IOException {
		if (args.length == 0)
			throw new IOException("Expected the shaded UltimateContent jar path");
		for (String argument : args) {
			Path jar = Path.of(argument).toAbsolutePath().normalize();
			normalizeJar(jar, System.out::println);
			verifyDistribution(jar);
		}
	}

	private static void verifyDistribution(Path path) throws IOException {
		try (JarFile jar = new JarFile(path.toFile())) {
			String root = "me/gamercoder215/mobchip/";
			for (String name : new String[] {"EntityBrain", "bukkit/BukkitBrain", "abstraction/ChipUtilFactory",
					"abstraction/v1_17_R1/ChipUtil1_17_R1", "abstraction/v1_21_R3/ChipUtil1_21_R3",
					"abstraction/v26_1/ChipUtil26_1", "abstraction/v26_2/ChipUtil26_2", "abstraction/v26_3/ChipUtil26_3"})
				if (jar.getJarEntry(root + name + ".class") == null)
					throw new IOException("Incomplete MobChip distribution: missing " + name);
			var entries = jar.entries();
			while (entries.hasMoreElements()) {
				JarEntry entry = entries.nextElement();
				if (!entry.getName().endsWith(".class"))
					continue;
				try (InputStream input = jar.getInputStream(entry)) {
					byte[] header = input.readNBytes(8);
					int major = header.length == 8 ? (Byte.toUnsignedInt(header[6]) << 8) | Byte.toUnsignedInt(header[7]) : -1;
					if (major < 0 || major > JAVA_21_CLASS_VERSION)
						throw new IOException("Older Paper remappers cannot read " + entry.getName() + " (class version " + major + ")");
				}
			}
		}
	}

	private static void normalizeJar(Path jar, Consumer<String> logger) throws IOException {
		Path temporary = Files.createTempFile(jar.getParent(), "ultimatecontent-normalized-", ".jar");
		int changed = 0;
		try {
			try (InputStream input = Files.newInputStream(jar);
					JarInputStream jarInput = new JarInputStream(input);
					OutputStream output = Files.newOutputStream(temporary);
					JarOutputStream jarOutput = jarInput.getManifest() == null
							? new JarOutputStream(output) : new JarOutputStream(output, jarInput.getManifest())) {
				JarEntry entry;
				while ((entry = jarInput.getNextJarEntry()) != null) {
					byte[] contents = jarInput.readAllBytes();
					if (NORMALIZABLE_PACKAGES.stream().anyMatch(entry.getName()::startsWith) && entry.getName().endsWith(".class")
							&& normalizeClassVersion(contents))
						changed++;
					writeEntry(jarOutput, entry, contents);
				}
			}
			if (changed == 0) {
				Files.deleteIfExists(temporary);
				return;
			}
			Files.move(temporary, jar, StandardCopyOption.REPLACE_EXISTING);
			logger.accept("[UltimateContent] Normalized " + changed + " MobChip 26.x classes in " + jar.getFileName()
					+ " for compatibility with older Paper remappers.");
		} finally {
			Files.deleteIfExists(temporary);
		}
	}

	private static boolean normalizeClassVersion(byte[] contents) throws IOException {
		if (contents.length < 8 || contents[0] != (byte) 0xCA || contents[1] != (byte) 0xFE
				|| contents[2] != (byte) 0xBA || contents[3] != (byte) 0xBE)
			throw new IOException("MobChip 26.x entry is not a valid class file");
		int minor = (Byte.toUnsignedInt(contents[4]) << 8) | Byte.toUnsignedInt(contents[5]);
		int major = (Byte.toUnsignedInt(contents[6]) << 8) | Byte.toUnsignedInt(contents[7]);
		if (minor != 0)
			throw new IOException("Cannot normalize preview MobChip bytecode with minor class version " + minor);
		if (major <= JAVA_21_CLASS_VERSION)
			return false;
		if (major != JAVA_25_CLASS_VERSION)
			throw new IOException("Cannot safely normalize unexpected MobChip class version " + major);
		contents[6] = 0;
		contents[7] = JAVA_21_CLASS_VERSION;
		return true;
	}

	private static void writeEntry(JarOutputStream output, JarEntry source, byte[] contents) throws IOException {
		JarEntry target = new JarEntry(source.getName());
		if (source.getComment() != null)
			target.setComment(source.getComment());
		if (source.getExtra() != null)
			target.setExtra(source.getExtra());
		if (source.getTime() >= 0)
			target.setTime(source.getTime());
		if (source.getMethod() == ZipEntry.STORED) {
			CRC32 crc = new CRC32();
			crc.update(contents);
			target.setMethod(ZipEntry.STORED);
			target.setSize(contents.length);
			target.setCompressedSize(contents.length);
			target.setCrc(crc.getValue());
		}
		output.putNextEntry(target);
		output.write(contents);
		output.closeEntry();
	}
}
