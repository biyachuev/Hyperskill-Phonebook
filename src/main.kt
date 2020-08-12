import java.io.File
import java.util.logging.*
import kotlin.math.*
import kotlin.random.Random

fun MutableList<String>.swap(index1: Int, index2: Int) {
    val tmp = this[index1]
    this[index1] = this[index2]
    this[index2] = tmp
}

object PhoneBook {
    private val directory = mutableListOf<String>()
    private val searchNames = mutableListOf<String>()
    private val logger = Logger.getLogger(PhoneBook::class.qualifiedName)
    private val fileHandler = FileHandler("phonebook.log")
    var directoryFileName = ""
    var searchNamesFileName = ""

//    var pivot: String = ""
//    var i: Int = 0

    private var t0: Long = 0
    private var t1: Long = 0

    object Result {
        var founded = 0
        var duration: Long = 0
        var text = ""
        var timeLimitExceeded: Boolean = false

        fun clear() {
            founded = 0
            duration = 0
            text = ""
            timeLimitExceeded = false
        }
    }

    init {
        logger.addHandler(fileHandler)
        logger.useParentHandlers = false
        fileHandler.formatter = SimpleFormatter()
    }

    private fun duration(dt: Long): String = String.format("%1\$tM min. %1\$tS sec. %1\$tL ms.", dt)

    private fun linearSearch() {
        Result.clear()
        t0 = System.currentTimeMillis()
        for (i in searchNames.indices) {
            for (j in directory.indices)
                if (searchNames[i] == directory[j].substringAfter(' ')) {
                    Result.founded++
                    break
                }
        }
        t1 = System.currentTimeMillis()
        Result.text = "Found ${Result.founded} / ${searchNames.size} entries. Time taken: ${duration(t1 - t0)}"
        Result.duration = t1 - t0
        Result.timeLimitExceeded = false
    }

    private fun jumpSearch() {
        Result.clear()
        t0 = System.currentTimeMillis()
        val step = sqrt(directory.size.toDouble()).toInt()
        for (i in searchNames.indices) {
            var start = 0
            loop@do {
                val end = min(start + step - 1, directory.lastIndex)
                if (directory[end].substringAfter(' ') >= searchNames[i]) {
                    for (j in end downTo start)
                        if (directory[j].substringAfter(' ') == searchNames[i]) {
                            Result.founded++
                            break@loop
                        }
                }
                start += step
            } while (start <= directory.lastIndex)
        }
        t1 = System.currentTimeMillis()
        Result.text = "Found ${Result.founded} / ${searchNames.size} entries. Time taken: ${duration(t1 - t0)}"
        Result.duration = t1 - t0
        Result.timeLimitExceeded = false
    }

    private fun binarySearch() {
        Result.clear()
        t0 = System.currentTimeMillis()

        for (i in searchNames.indices) {
            var left = 0
            var right = directory.lastIndex
            loop@ do {
                val mid = (left + right) / 2
                if (directory[mid].substringAfter(' ') < searchNames[i]) left = mid + 1
                else if (directory[mid].substringAfter(' ') > searchNames[i]) right = mid - 1
                else {
                    Result.founded++
                    break@loop
                }
            } while (left <= right)
        }

        t1 = System.currentTimeMillis()
        Result.text = "Found ${Result.founded} / ${searchNames.size} entries. Time taken: ${duration(t1 - t0)}"
        Result.duration = t1 - t0
        Result.timeLimitExceeded = false
    }

    private fun qsortLomuto(left: Int, right: Int) {
        if (left < right) {
            var i = left
            val pivot = directory[right].substringAfter(' ') // in Lomuto pivot = rightmost element
            for (j in left until right)
                if (directory[j].substringAfter(' ') <= pivot) directory.swap(i++, j)
            directory.swap(i, right)
            qsortLomuto(left, i - 1)
            qsortLomuto(i + 1, right)
        }
    }

    private fun quickSort() {
        Result.clear()
        t0 = System.currentTimeMillis()
        qsortLomuto(0, directory.lastIndex)

        t1 = System.currentTimeMillis()
        Result.text = "Sorting time: ${duration(t1 - t0)}"
        Result.duration = t1 - t0

        logger.info("first element of sorted directory = ${directory[0]}")
        logger.info("last element of sorted directory = ${directory[directory.lastIndex]}")
//        for (i in directory) logger.info("directory[element]: $i")
    }

    private fun bubbleSort(timeLimit: Long) {
        Result.clear()
        t0 = System.currentTimeMillis()
        var noSwaps = true
        for (i in 0 until directory.size - 1) {
            for (j in 1 until directory.size - i)
            if (directory[j].substringAfter(' ') < directory[j - 1].substringAfter(' ')) {
                directory[j] = directory[j-1].also { directory[j-1] = directory[j] }
                noSwaps = false
            }
            if (noSwaps) break else noSwaps = true
            if (System.currentTimeMillis() - t0 > timeLimit) {
                Result.timeLimitExceeded = true
                break
            }
        }
        t1 = System.currentTimeMillis()
        Result.text = "Sorting time: ${duration(t1 - t0)}"
        Result.duration = t1 - t0

        logger.info("first element of sorted directory = ${directory[0]}")
        logger.info("last element of sorted directory = ${directory[directory.lastIndex]}")
//        for (i in directory) logger.info("directory[element]: $i")
    }

    fun search() {
        var temp = ""
        var totalDuration: Long = 0

        t0 = System.currentTimeMillis()
        File(directoryFileName).forEachLine { directory.add(it.trim()) }
        File(searchNamesFileName).forEachLine { searchNames.add(it.trim()) }
        t1 = System.currentTimeMillis()

        logger.info("Both files downloaded in ${duration(t1 - t0)}")
        logger.info("Directory size = ${directory.size}")
        logger.info("Search list size = ${searchNames.size}")

        println("Start searching (linear search)...")
        linearSearch()  // used to stop too slow bubble search
        val timeLimit = Result.duration * 10
        println(Result.text)

        println("Start searching (bubble sort + jump search)...")
        bubbleSort(timeLimit)
        totalDuration = Result.duration
        if (Result.timeLimitExceeded) {
            temp += "${Result.text} - STOPPED, moved to linear search\n"
            linearSearch()
            temp += "Searching time: ${duration(Result.duration)}"
        } else {
            temp += Result.text + "\n"
            jumpSearch()
            temp += "Searching time: ${duration(Result.duration)}"
        }
        totalDuration += Result.duration
        println("Found ${Result.founded} / ${searchNames.size} entries. Time taken: ${duration(totalDuration)}")
        println(temp)

        directory.clear()
        File(directoryFileName).forEachLine { directory.add(it.trim()) }

        println("Start searching (quick sort + binary search)...")
        quickSort()
        totalDuration = Result.duration
        temp = Result.text + "\n"
        binarySearch()
        temp += "Searching time: ${duration(Result.duration)}"
        totalDuration += Result.duration
        println("Found ${Result.founded} / ${searchNames.size} entries. Time taken: ${duration(totalDuration)}")
        println(temp)

    }

    fun generateTestSets(percentDirElements: Int, percentSearchElements: Int) {
        if (percentDirElements > 0) {
            if (directory.isEmpty()) File(directoryFileName).forEachLine { directory.add(it.trim()) }
            val dirFile = File("inputData/directory${percentDirElements}.txt")
            dirFile.writeText(directory[Random.nextInt(directory.size)] + "\n")
            for (i in 1 until directory.size * percentDirElements / 100) {
                dirFile.appendText(directory[Random.nextInt(directory.size)] + "\n")
            }
        }
        if (percentSearchElements > 0) {
            if (searchNames.isEmpty()) File(searchNamesFileName).forEachLine { searchNames.add(it.trim()) }
            val searchFile = File("inputData/find${percentSearchElements}.txt")
            searchFile.writeText(searchNames[Random.nextInt(searchNames.size)] + "\n")
            for (i in 1 until searchNames.size * percentSearchElements / 100) {
                searchFile.appendText(searchNames[Random.nextInt(searchNames.size)] + "\n")
            }
        }
    }
}

fun main() {
    PhoneBook.directoryFileName = "/Users/biyachuev/Documents/Projects_Kotlin/Hyperskill_Phonebook_hard/inputData/directory.txt"
    PhoneBook.searchNamesFileName = "/Users/biyachuev/Documents/Projects_Kotlin/Hyperskill_Phonebook_hard/inputData/find.txt"
    PhoneBook.search()
//    PhoneBook.generateTestSets(5, -1)
}