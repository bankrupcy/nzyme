/*
 *  This file is part of nzyme.
 *
 *  nzyme is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  nzyme is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with nzyme.  If not, see <http://www.gnu.org/licenses/>.
 */

package horse.wtf.nzyme.bandits.identifiers;

import horse.wtf.nzyme.notifications.FieldNames;
import horse.wtf.nzyme.util.Tools;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class BanditIdentifierFactory {

    public static BanditIdentifier create(BanditIdentifier.TYPE type, Map<String, Object> config, @Nullable Long databaseID, UUID uuid) throws NoSerializerException, MappingException {
        switch (type) {
            case FINGERPRINT:
                if (!config.containsKey(FieldNames.FINGERPRINT)) {
                    throw new MappingException(config.toString());
                }

                try {
                    return new FingerprintBanditIdentifier((String) config.get(FieldNames.FINGERPRINT), databaseID, uuid);
                } catch(Exception e) {
                    throw new MappingException(config.toString(), e);
                }
            case SSID:
                if (!config.containsKey(FieldNames.SSIDS)) {
                    throw new MappingException(config.toString());
                }

                try {
                    //noinspection unchecked
                    return new SSIDIBanditdentifier((List<String>) config.get(FieldNames.SSIDS), databaseID, uuid);
                } catch(Exception e) {
                    throw new MappingException(config.toString(), e);
                }
            case SIGNAL_STRENGTH:
                if (!config.containsKey(FieldNames.FROM) || !config.containsKey(FieldNames.TO)) {
                    throw new MappingException(config.toString());
                }

                try {
                    return new SignalStrengthBanditIdentifier(
                            Tools.getInteger(config.get(FieldNames.FROM)),
                            Tools.getInteger(config.get(FieldNames.TO)),
                            databaseID,
                            uuid
                    );
                } catch(Exception e) {
                    throw new MappingException(config.toString(), e);
                }
            case PWNAGOTCHI_IDENTITY:
                if (!config.containsKey(FieldNames.IDENTITY)) {
                    throw new MappingException(config.toString());
                }

                try {
                    return new PwnagotchiBanditIdentifier((String) config.get(FieldNames.IDENTITY), databaseID, uuid);
                } catch(Exception e) {
                    throw new MappingException(config.toString(), e);
                }
            default:
                throw new NoSerializerException();
        }
    }

    public static class NoSerializerException extends Exception {
    }

    public static class MappingException extends Exception {

        public MappingException(String msg) {
            super(msg);
        }

        public MappingException(String msg, Throwable e) {
            super(msg, e);
        }

    }
}
