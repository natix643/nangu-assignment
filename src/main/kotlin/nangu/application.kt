package nangu

import nangu.Role.*
import org.springframework.boot.*
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.*
import org.springframework.security.config.http.SessionCreationPolicy.STATELESS
import org.springframework.security.core.userdetails.*
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder

fun main(args: Array<String>) {
    runApplication<Application>(*args)
}

@SpringBootApplication
class Application {

    @Bean
    fun initDb(
        userRepository: UserRepository,
        messageRepository: MessageRepository,
        passwordEncoder: PasswordEncoder
    ) = ApplicationRunner {
        val user1 = User(username = "jiri", password = "heslo", role = ADMIN)
        val user2 = User(username = "jarda", password = "secret", role = USER)

        val usersWithEncodedPasswords = listOf(user1, user2)
            .map { it.copy(password = passwordEncoder.encode(it.password)) }
        userRepository.saveAll(usersWithEncodedPasswords)

        messageRepository.saveAll(listOf(
            Message(text = "hello world", author = user1.username),
            Message(text = "this is a test", author = user2.username)
        ))
    }
}

@EnableWebSecurity
class SecurityConfig(val userRepository: UserRepository) : WebSecurityConfigurerAdapter() {

    override fun configure(http: HttpSecurity) {
        http
            .httpBasic()
            .and()
            .csrf().disable()
            .sessionManagement().sessionCreationPolicy(STATELESS)
            .and()
            .authorizeRequests().anyRequest().authenticated()
    }

    override fun configure(auth: AuthenticationManagerBuilder) {
        auth.userDetailsService(userDetailsService())
            .passwordEncoder(passwordEncoder())
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Bean
    override fun userDetailsService() = UserDetailsService { username ->
        val user = userRepository.findByUsername(username) ?: throw UsernameNotFoundException(username)

        org.springframework.security.core.userdetails.User
            .withUsername(user.username)
            .password(user.password)
            .roles(user.role.toString())
            .build()
    }
}
