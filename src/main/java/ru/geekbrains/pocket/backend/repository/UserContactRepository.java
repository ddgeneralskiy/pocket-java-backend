package ru.geekbrains.pocket.backend.repository;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import ru.geekbrains.pocket.backend.domain.db.User;
import ru.geekbrains.pocket.backend.domain.db.UserContact;

import java.util.List;

@Repository
public interface UserContactRepository extends MongoRepository<UserContact, ObjectId> {

    List<UserContact> findByUser(User user);

    UserContact findFirstByUserAndContact(User user, User contact);

    //@Query("{ '_id': ?0, 'userId': ?1 }")
    UserContact findFirstByUserAndContact_Id(User user, ObjectId id);

    void deleteByUser(User user);

}
