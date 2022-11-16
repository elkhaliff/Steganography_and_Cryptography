package cryptography

fun getString(message: String): String {
    println(message)
    return readln()
}

private fun getTask() = getString("Task (hide, show, exit):")

fun main() {
    while (true) {
        val task = getTask()
        if (task == "exit") break
        menu(task)
    }
    println("Bye!")
}

private fun menu(command: String) {
    val crypto = Crypto()
    when (command) {
        "hide" -> crypto.hide()
        "show" -> crypto.show()
        else -> "Wrong task: $command"
    }.let { println(it) }
}