import com.github.zly2006.synapse.Entity

@Entity(tableName = "person")
class Person(
    val name: String,
    val age: Int,
    val address: String
)


fun main() {
    /**
     * The compiler plugin will replace this with create<MyTest>(_MyTestProvider)
     */
//    val myTest = create<MyTest>()
//    myTest.print()
}
