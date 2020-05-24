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

package horse.wtf.nzyme.bandits.database;

import horse.wtf.nzyme.bandits.Contact;
import horse.wtf.nzyme.database.Database;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.joda.time.DateTime;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class ContactMapper implements RowMapper<Contact> {

    @Override
    public Contact map(ResultSet rs, StatementContext ctx) throws SQLException {
        return Contact.create(
                UUID.fromString(rs.getString("contact_uuid")),
                rs.getLong("bandit_id"),
                null,
                DateTime.parse(rs.getString("first_seen"), Database.DATE_TIME_FORMATTER),
                DateTime.parse(rs.getString("last_seen"), Database.DATE_TIME_FORMATTER),
                rs.getLong("frame_count")
        );
    }

}
