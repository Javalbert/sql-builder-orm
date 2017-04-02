/*
 * This file is generated by jOOQ.
*/
package org.jooq.javalbert;


import javax.annotation.Generated;

import org.jooq.Identity;
import org.jooq.UniqueKey;
import org.jooq.impl.AbstractKeys;
import org.jooq.javalbert.tables.Datatypeholder;
import org.jooq.javalbert.tables.records.DatatypeholderRecord;


/**
 * A class modelling foreign key relationships between tables of the <code>PUBLIC</code> 
 * schema
 */
@Generated(
    value = {
        "http://www.jooq.org",
        "jOOQ version:3.9.1"
    },
    comments = "This class is generated by jOOQ"
)
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Keys {

    // -------------------------------------------------------------------------
    // IDENTITY definitions
    // -------------------------------------------------------------------------

    public static final Identity<DatatypeholderRecord, Long> IDENTITY_DATATYPEHOLDER = Identities0.IDENTITY_DATATYPEHOLDER;

    // -------------------------------------------------------------------------
    // UNIQUE and PRIMARY KEY definitions
    // -------------------------------------------------------------------------

    public static final UniqueKey<DatatypeholderRecord> CONSTRAINT_C = UniqueKeys0.CONSTRAINT_C;

    // -------------------------------------------------------------------------
    // FOREIGN KEY definitions
    // -------------------------------------------------------------------------


    // -------------------------------------------------------------------------
    // [#1459] distribute members to avoid static initialisers > 64kb
    // -------------------------------------------------------------------------

    private static class Identities0 extends AbstractKeys {
        public static Identity<DatatypeholderRecord, Long> IDENTITY_DATATYPEHOLDER = createIdentity(Datatypeholder.DATATYPEHOLDER, Datatypeholder.DATATYPEHOLDER.ID);
    }

    private static class UniqueKeys0 extends AbstractKeys {
        public static final UniqueKey<DatatypeholderRecord> CONSTRAINT_C = createUniqueKey(Datatypeholder.DATATYPEHOLDER, "CONSTRAINT_C", Datatypeholder.DATATYPEHOLDER.ID);
    }
}