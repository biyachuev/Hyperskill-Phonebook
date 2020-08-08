import java.io.File
import java.util.logging.*

object PhoneBook {
    val numbers = mutableListOf<String>()
    val names = mutableListOf<String>()
    val searchNames = mutableListOf<String>()
    private val logger = Logger.getLogger(PhoneBook::class.qualifiedName)
    private val fileHandler = FileHandler("phonebook.log")
    var result = ""

    init {
        logger.addHandler(fileHandler)
        logger.useParentHandlers = false
        fileHandler.formatter = SimpleFormatter()
    }

    fun timeDiff(t0: Long, t1: Long): String {
        val dt = t1 - t0
        return String.format("%1\$tM min. %1\$tS sec. %1\$tL ms.", dt)
    }

    fun linearSearch() {

    }

    fun search(dir: String, searchList: String) {
        var time0 = System.currentTimeMillis()
        File(dir).forEachLine {
            numbers.add(it.trim().substringBefore(' '))
            names.add(it.trim().substringAfter(' '))
        }
        File(searchList).forEachLine {
            searchNames.add(it.trim())
        }

        logger.info("Both files downloaded in ${timeDiff(time0, System.currentTimeMillis())}")
        println("Start searching...")
        time0 = System.currentTimeMillis()

        var founded = 0
        for (i in searchNames.indices) {
            for (j in names.indices)
                if (searchNames[i] == names[j]) {
                    founded++
                    break
                }
        }

        val time1 = System.currentTimeMillis()
        println("Found $founded / ${searchNames.size} entries. Time taken: ${timeDiff(time0, time1)}")
    }
}

fun main() {
    PhoneBook.search("/Users/biyachuev/Documents/Projects_Kotlin/Hyperskill_Phonebook_hard/directory.txt",
        "/Users/biyachuev/Documents/Projects_Kotlin/Hyperskill_Phonebook_hard/find.txt")
//    println(PhoneBook.returnTimeDiff(0, 66666))
}