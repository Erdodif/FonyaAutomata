import State.Q0
import State.Q1
import State.Q2
import State.Q3
import State.Q4
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.staticCFunction
import platform.posix.SIGALRM
import platform.posix.SIGBUS
import platform.posix.SIGCLD
import platform.posix.SIGINT
import platform.posix.SIGTERM
import platform.posix.SIGUSR1
import platform.posix.SIGUSR2
import platform.posix.exit
import platform.posix.getpid
import platform.posix.signal
import platform.posix.stat

@OptIn(ExperimentalForeignApi::class)
val STDERR = platform.posix.fdopen(2, "w")

var lastSent = 0

@OptIn(ExperimentalForeignApi::class)
fun handleSignal(sigNum: Int) {
    val sigString = when (sigNum) {
        SIGINT -> "SIGINT"
        SIGBUS -> "SIGBUS"
        SIGTERM -> "SIGTERM"
        SIGUSR1 -> "SIGUSR1"
        SIGUSR2 -> "SIGUSR2"
        SIGALRM -> "SIGALRM"
        SIGCLD -> "SIGCLD"
        else -> "unknown"
    }
    platform.posix.fprintf(STDERR, "%s got catched, send again to exit\n", sigString)
    platform.posix.fflush(STDERR)
    if (sigNum == lastSent) {
        exit(0)
    }
    lastSent = sigNum
}

enum class State {
    Q0,
    Q1,
    Q2,
    Q3,
    Q4
}

fun validWord(word: String): Boolean {
    var state = Q0
    for (char in word) {
        when (state) {
            Q0 -> {
                state = if (char == '"') Q1
                else Q2
            }

            Q1 -> {
                state = when (char) {
                    '\\' -> Q4
                    '"' -> Q3
                    else -> Q1
                }
            }

            Q2 -> state = Q2

            Q3 -> state = Q2

            Q4 -> {
                state = if (char in "\"\\nt") Q1
                else Q2
            }
        }
    }

    return state == Q3
}

@OptIn(ExperimentalForeignApi::class)
fun main() {
    for (sig in listOf(SIGINT, SIGBUS, SIGTERM, SIGUSR1, SIGUSR2, SIGALRM, SIGCLD)) {
        signal(sig, staticCFunction(::handleSignal))
    }
    val pid = getpid()
    println("Launched with PID: $pid")
    while (true) {
        print("Következő szó: ")
        val string = readln()
        if(validWord(string)){

            println("\u001b[1;32mA szó helyes.\u001b[0m")
        }
        else{
            println("\u001B[1;31mA szó nem helyes!\u001B[0m")
        }
    }
}