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

package horse.wtf.nzyme.notifications.uplinks.graylog;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class GraylogAddress {

    public abstract String host();
    public abstract int port();

    public static GraylogAddress create(String host, int port) {
        return builder()
                .host(host)
                .port(port)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_GraylogAddress.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder host(String host);

        public abstract Builder port(int port);

        public abstract GraylogAddress build();
    }

}
