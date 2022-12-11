package horse.wtf.nzyme.crypto;

import com.google.common.base.Strings;
import horse.wtf.nzyme.MockNzyme;
import horse.wtf.nzyme.NzymeLeader;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.testng.Assert.*;

public class CryptoTest {

    private static final Path FOLDER = Paths.get("crypto_test");

    @SuppressWarnings({"resource", "ResultOfMethodCallIgnored"})
    @BeforeMethod
    public void cleanDirectory() throws IOException {
        Files.walk(FOLDER)
                .map(Path::toFile)
                .forEach(file -> {
                    // Don't delete the entire crypto_test root directory.
                    if (!file.toPath().equals(FOLDER)) {
                        file.delete();
                    }
                });

        long size = Files.walk(FOLDER)
                .filter(p -> p.toFile().isFile())
                .mapToLong(p -> p.toFile().length())
                .sum();

        assertEquals(size, 0, "Crypto key test folder is not empty.");
    }

    private String readKeyIdFromDB(NzymeLeader nzyme) {
        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT key_signature FROM crypto_keys " +
                        "WHERE node = :node AND key_type = 'PGP'")
                        .bind("node", nzyme.getNodeID())
                        .mapTo(String.class)
                        .one()
        );
    }

    @Test
    public void testInitialize() throws Crypto.CryptoInitializationException, IOException {
        NzymeLeader mockNzyme = new MockNzyme();

        File secretFile = Paths.get(FOLDER.toString(), "secret.asc").toFile();
        File publicFile = Paths.get(FOLDER.toString(), "public.asc").toFile();

        new Crypto(mockNzyme).initialize();

        assertTrue(secretFile.exists());
        assertTrue(publicFile.exists());

        assertTrue(Files.readString(secretFile.toPath()).startsWith("-----BEGIN PGP PRIVATE KEY BLOCK-----"));
        assertTrue(Files.readString(publicFile.toPath()).startsWith("-----BEGIN PGP PUBLIC KEY BLOCK-----"));
        assertFalse(Strings.isNullOrEmpty(readKeyIdFromDB(mockNzyme)));
    }

    @Test
    public void testInitializeDoesNotRegenerateKeysOnEachInit() throws Crypto.CryptoInitializationException, IOException {
        NzymeLeader mockNzyme = new MockNzyme();

        Path secretPath = Paths.get(FOLDER.toString(), "secret.asc");
        Path publicPath = Paths.get(FOLDER.toString(), "public.asc");

        new Crypto(mockNzyme).initialize();
        String secret1 = Files.readString(secretPath);
        String public1 = Files.readString(publicPath);
        String sig1 = readKeyIdFromDB(mockNzyme);

        new Crypto(mockNzyme).initialize();
        String secret2 = Files.readString(secretPath);
        String public2 = Files.readString(publicPath);
        String sig2 = readKeyIdFromDB(mockNzyme);

        assertEquals(secret1, secret2);
        assertEquals(public1, public2);
        assertNotNull(sig1);
        assertNotNull(sig2);
        assertEquals(sig1, sig2);
    }

    @Test
    public void testInitializeRegeneratesKeysIfSecretMissing() throws Crypto.CryptoInitializationException, IOException {
        NzymeLeader mockNzyme = new MockNzyme();

        Path secretPath = Paths.get(FOLDER.toString(), "secret.asc");
        Path publicPath = Paths.get(FOLDER.toString(), "public.asc");

        new Crypto(mockNzyme).initialize();
        String secret1 = Files.readString(secretPath);
        String public1 = Files.readString(publicPath);
        String sig1 = readKeyIdFromDB(mockNzyme);

        secretPath.toFile().delete();

        new Crypto(mockNzyme).initialize();
        String secret2 = Files.readString(secretPath);
        String public2 = Files.readString(publicPath);
        String sig2 = readKeyIdFromDB(mockNzyme);

        assertNotEquals(secret1, secret2);
        assertNotEquals(public1, public2);
        assertNotNull(sig1);
        assertNotNull(sig2);
        assertNotEquals(sig1, sig2);
    }

    @Test
    public void testInitializeRegeneratesKeysIfPublicMissing() throws Crypto.CryptoInitializationException, IOException {
        NzymeLeader mockNzyme = new MockNzyme();

        Path secretPath = Paths.get(FOLDER.toString(), "secret.asc");
        Path publicPath = Paths.get(FOLDER.toString(), "public.asc");

        new Crypto(mockNzyme).initialize();
        String secret1 = Files.readString(secretPath);
        String public1 = Files.readString(publicPath);
        String sig1 = readKeyIdFromDB(mockNzyme);

        publicPath.toFile().delete();

        new Crypto(mockNzyme).initialize();
        String secret2 = Files.readString(secretPath);
        String public2 = Files.readString(publicPath);
        String sig2 = readKeyIdFromDB(mockNzyme);

        assertNotEquals(secret1, secret2);
        assertNotEquals(public1, public2);
        assertNotNull(sig1);
        assertNotNull(sig2);
        assertNotEquals(sig1, sig2);
    }

    @Test
    public void testInitializeRegeneratesKeysIfPublicAndSecretMissing() throws Crypto.CryptoInitializationException, IOException {
        NzymeLeader mockNzyme = new MockNzyme();

        Path secretPath = Paths.get(FOLDER.toString(), "secret.asc");
        Path publicPath = Paths.get(FOLDER.toString(), "public.asc");

        new Crypto(mockNzyme).initialize();
        String secret1 = Files.readString(secretPath);
        String public1 = Files.readString(publicPath);
        String sig1 = readKeyIdFromDB(mockNzyme);

        secretPath.toFile().delete();
        publicPath.toFile().delete();

        new Crypto(mockNzyme).initialize();
        String secret2 = Files.readString(secretPath);
        String public2 = Files.readString(publicPath);
        String sig2 = readKeyIdFromDB(mockNzyme);

        assertNotEquals(secret1, secret2);
        assertNotEquals(public1, public2);
        assertNotNull(sig1);
        assertNotNull(sig2);
        assertNotEquals(sig1, sig2);
    }

}