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

package horse.wtf.nzyme.configuration.base;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigException;
import com.typesafe.config.ConfigFactory;
import horse.wtf.nzyme.Role;
import horse.wtf.nzyme.configuration.ConfigurationKeys;
import horse.wtf.nzyme.configuration.ConfigurationValidator;
import horse.wtf.nzyme.configuration.IncompleteConfigurationException;
import horse.wtf.nzyme.configuration.InvalidConfigurationException;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Files;

public class BaseConfigurationLoader {

    private final Config general;

    public BaseConfigurationLoader(File configFile) throws InvalidConfigurationException, IncompleteConfigurationException, FileNotFoundException {
        if (!Files.isReadable(configFile.toPath())) {
            throw new FileNotFoundException("File at [" + configFile.getPath() + "] does not exist or is not readable. Check path and permissions.");
        }

        Config root = ConfigFactory.parseFile(configFile).resolve();

        try {
            this.general = root.getConfig(ConfigurationKeys.GENERAL);
        } catch(ConfigException e) {
            throw new IncompleteConfigurationException("Incomplete configuration.", e);
        }

        validate();
    }

    public BaseConfiguration get() {
        return BaseConfiguration.create(parseRole());
    }

    public Role parseRole() {
        return general.getEnum(Role.class, ConfigurationKeys.ROLE);
    }

    public void validate() throws InvalidConfigurationException, IncompleteConfigurationException {
        ConfigurationValidator.expectEnum(general, ConfigurationKeys.ROLE, ConfigurationKeys.GENERAL, Role.class);
    }


}
