package nangu

import org.springframework.data.jpa.repository.*
import org.springframework.data.jpa.repository.Query
import javax.persistence.*
import javax.persistence.EnumType.STRING

@Entity
data class Message(
    @Id
    @GeneratedValue
    val id: Long? = null,

    @Column(nullable = false)
    val text: String,

    @Column(nullable = false)
    val author: String
)

interface MessageRepository : JpaRepository<Message, Long> {

    @Query("from Message m where (m.text like %:text%) and (m.author = :author or :author = null)")
    fun findAllByTextAndAuthor(text: String, author: String?): List<Message>
}

@Entity
data class User(
    @Id
    @GeneratedValue
    val id: Long? = null,

    @Column(nullable = false)
    val username: String,

    @Column(nullable = false)
    val password: String,

    @Column(nullable = false)
    @Enumerated(STRING)
    val role: Role
)

enum class Role {
    USER, ADMIN
}

interface UserRepository : JpaRepository<User, Long> {
    fun findByUsername(name: String): User?
}
