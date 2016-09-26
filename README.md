## Mongo Update Embedded Plugin for Grails 3.1.x

Latest Version (0.0.1)

### Installation 
 
Add this to the dependencies block of your `build.gradle` file.

```
#!groovy

dependencies {
    compile "com.causecode.plugins:mongo-update-embedded:$VERSION" 
}
```

### Usage

The MongoDB database does not support the concept of joins, hence as an alternative, we use the concept of embedded documents.
Refer to the [Grails docs for embedded](http://docs.grails.org/3.2.0.RC1/ref/Domain%20Classes/embedded.html).

Embedding entire domain objects within another domain object would make the data redundant. If you just use the reference of the object
and does not embed it (using the static embedded field), then you will have to do multiple queries to fetch the object that is referenced.

To avoid redundant data in the database as well as multiple queries, we can create simple Groovy classes under `src/main/groovy`
with the most relevant fields of the domain class and then embed them as an embedded object of the parent domain class.

In such case, whenever any domain object is updated, it's embedded object in some other domain remains unchanged. This plugin solves this
problem by updating those embedded instances.

### Implementing in your Grails App

You will have to create a class that will represent embedded objects of the parent domain class.

This class needs to implement the trait `EmbeddableDomain` which contains some necessary methods to treat a class as an embeddable class.
    
**Example:** For a domain class `User` you can create an embedded class called `EmUser` under `src/main/groovy`.
            
```
#!groovy

class EmUser implements EmbeddableDomain {
    
    ObjectId instanceId
    String email
    String username
        
    EmUser(ObjectId instanceId, String username, String email) {
        this.instanceId = instanceId
        this.username = username
        this.email = email
    }
}
```

**_Important:_** The field name representing the `id` of the domain class within the embedded class needs to be `instanceId`, else it will not work.
    
**Note:** The default implementation assumes the name format to be `Em` prefixed to the domain class name. But you can create your own naming
    convention by overriding the `resolveParentDomainClass` method which returns the domain name of the actual parent domain.

Your domain class should have a method named `getEmbeddedInstance` which returns an object of the embeddable class for a domain instance. 
 
```
#!groovy

class User {
    
    ObjectId id
    String email
    String username
        
    // Other fields here...
        
    EmUser getEmbeddedInstance() {
        return new EmUser(this.id, this.username, this.email)
    }
}
```

Finally, you can embed this class's object using the `static embedded` fields in any other domain class.

**Example:** For a domain class `UserProfile`, you can create a field `EmUser user` and then mark the field as embedded as
    `static embedded = ['user']`.
    
**Note:** If you change the field name mapping using `static mapping` block, the plugin will not update the embedded instance.