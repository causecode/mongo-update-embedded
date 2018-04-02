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
