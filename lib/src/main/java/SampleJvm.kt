import com.github.zly2006.synapse.Entity

@Entity(tableName = "person")
class Person(
    val name: String,
    val age: Int,
    val address: String,
) {
    companion object {
        external fun hello(): String
        external fun printExpr(expr: (Person) -> Unit): String
    }
}


fun main() {
    Person("Alice", 20, "Wonderland")
    println(Person.printExpr {
        it.age >= 35
    })
}
