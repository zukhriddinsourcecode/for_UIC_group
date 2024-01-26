import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.List;

@SpringBootApplication
public class UserInfoApi {

    public static void main(String[] args) {
        SpringApplication.run(UserInfoApi.class, args);
    }
}

@RestController
@RequestMapping("/user-info")
class UserInfoController {

    private final UserInfoService userInfoService;

    @Autowired
    public UserInfoController(UserInfoService userInfoService) {
        this.userInfoService = userInfoService;
    }

    @GetMapping
    public List<UserInfo> getUserInfo() {
        return userInfoService.getUserInfo();
    }
}

@Entity
@Data
class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String fio;
    private String email;
    private Integer phone;
    private String status;
    private String dob;
    private LocalDate dt;

}

@Entity
@Data
class Card {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String cardName;
    private String cardType;
    private String cardNumber;
    private String cardExpire;
    private Integer balance;
    private String status;
    private LocalDate dt;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}

@Entity
@Data
class CardTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String type;
    private Integer amount;
    private Integer oldBalance;
    private Integer newBalance;
    private LocalDate dt;

    @ManyToOne
    @JoinColumn(name = "card_id")
    private Card card;
}

interface UserRepository extends JpaRepository<User, Long> {
}

interface CardRepository extends JpaRepository<Card, Long> {
    List<Card> findByUser(User user);
}

interface CardTransactionRepository extends JpaRepository<CardTransaction, Long> {
    List<CardTransaction> findByCard(Card card);
}

class UserInfo {

    private String fio;
    private List<CardInfo> cards;

    public UserInfo(String fio, List<CardInfo> cards) {
        this.fio = fio;
        this.cards = cards;
    }
}

class CardInfo {

    private List<TransactionInfo> transactions;

    public CardInfo(List<TransactionInfo> transactions) {
        this.transactions = transactions;
    }
}

class TransactionInfo {

    private String type;
    private Integer amount;
    private Integer oldBalance;
    private Integer newBalance;
    private LocalDate transactionDate;

    public TransactionInfo(String type, Integer amount, Integer oldBalance, Integer newBalance, LocalDate transactionDate) {
        this.type = type;
        this.amount = amount;
        this.oldBalance = oldBalance;
        this.newBalance = newBalance;
        this.transactionDate = transactionDate;
    }
}

@Service
class UserInfoService {

    private final UserRepository userRepository;
    private final CardRepository cardRepository;
    private final CardTransactionRepository cardTransactionRepository;

    @Autowired
    public UserInfoService(UserRepository userRepository, CardRepository cardRepository, CardTransactionRepository cardTransactionRepository) {
        this.userRepository = userRepository;
        this.cardRepository = cardRepository;
        this.cardTransactionRepository = cardTransactionRepository;
    }

    public List<UserInfo> getUserInfo() {
        List<User> users = userRepository.findAll();
        return users.stream()
                .map(this::convertToUserInfo)
                .toList();
    }

    private UserInfo convertToUserInfo(User user) {
        List<Card> cards = cardRepository.findByUser(user);
        List<CardInfo> cardInfos = cards.stream()
                .map(this::convertToCardInfo)
                .toList();

        return new UserInfo(user.getFio(), cardInfos);
    }

    private CardInfo convertToCardInfo(Card card) {
        List<CardTransaction> transactions = cardTransactionRepository.findByCard(card);
        List<TransactionInfo> transactionInfos = transactions.stream()
                .map(this::convertToTransactionInfo)
                .toList();

        return new CardInfo(transactionInfos);
    }

    private TransactionInfo convertToTransactionInfo(CardTransaction transaction) {
        return new TransactionInfo(
                transaction.getType(),
                transaction.getAmount(),
                transaction.getOldBalance(),
                transaction.getNewBalance(),
                transaction.getDt()
        );
    }
}
