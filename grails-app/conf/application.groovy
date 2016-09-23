/*
 * Copyright (c) 2016, CauseCode Technologies Pvt Ltd, India.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or
 * without modification, are not permitted.
 */

environments {
    test {
        grails {
            mongodb {
                engine = 'mapping'
                port = 27018
                databaseName = 'mongo_update_test'
            }
        }
    }
}