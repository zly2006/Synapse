# Synapse

Ultimate Object Relational Mapper for Kotlin.

## Example

```kotlin
@Entity(tableName = "person")
class Person(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val name: String,
    val age: Int,
    val address: String
)

fun foo() {
    // Auto generated constructor of Person(String, Int, String)
    Person.insert(Person("John", 30, "New York"))
    val johnAge = Person.where {
        it.name == "John"
    }.age // Auto generated getter of Person.age: SqlList<Int>
        .firstOrNull()
}
```
