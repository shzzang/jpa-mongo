package com.omi.back.controller;

import com.mongodb.client.result.UpdateResult;
import com.omi.back.domain.ResultEntity;
import com.omi.back.domain.User;
import com.omi.back.exception.UserEmailExistException;
import com.omi.back.exception.UserNotFoundException;
import com.omi.back.service.SequenceGeneratorService;
import com.omi.back.util.Constant;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Date;
import java.util.Objects;

import static org.springframework.data.mongodb.core.query.Criteria.where;

@RestController
@RequestMapping("/user")
public class UserController {

    private MongoTemplate mongoTemplate;
    private SequenceGeneratorService sequenceGeneratorService;

    public UserController(MongoTemplate mongoTemplate, SequenceGeneratorService sequenceGeneratorService) {
        this.mongoTemplate = mongoTemplate;
        this.sequenceGeneratorService = sequenceGeneratorService;
    }

    /**
     * 유저 리스트
     * @return
     */
    @GetMapping("/list")
    public ResultEntity getUserList() {
        Query query = new Query(where("isSecession").ne(true));
        query.with(Sort.by(Sort.Direction.DESC, "_id"));
        return new ResultEntity(Constant.SUCCESS_CODE, mongoTemplate.find(query, User.class));
    }

    /**
     * 유저 정보
     * @param findUser
     * @return
     */
    @GetMapping("/info")
    public ResultEntity getUser(@RequestBody @Valid User findUser) {
        Query query = new Query();
        query.addCriteria(where("email").is(findUser.getEmail()).and("isSecession").is(false));

        User user = mongoTemplate.findOne(query, User.class);

        if (Objects.isNull(user)) {
            throw new UserNotFoundException();
        }

        if (BCrypt.checkpw(findUser.getPassword(), user.getPassword())) {
            return new ResultEntity(Constant.SUCCESS_CODE, user);
        }

        throw new UserNotFoundException();
    }

    /**
     * 유저 정보 업데이트
     * @param newUser
     * @return
     */
    @PutMapping("/info")
    public ResultEntity modifyUser(@RequestBody User newUser) {
        Query query = new Query(where("isSecession").ne(true).and("_id").is(newUser.getId()));

        User user = mongoTemplate.findOne(query, User.class);
        if (Objects.isNull(user)) {
            throw new UserNotFoundException();
        }

        Update update = new Update();
        update.set("updateAt", new Date());
        update.set("name", newUser.getName());
        update.set("password", BCrypt.hashpw(newUser.getPassword(), BCrypt.gensalt()));

        UpdateResult updateResult = mongoTemplate.updateFirst(query, update, User.class);

        return new ResultEntity(updateResult.wasAcknowledged() ? Constant.SUCCESS_CODE : Constant.FAILURE_CODE, mongoTemplate.findById(newUser.getId(), User.class));
    }

    /**
     * 유저 생성
     * @param user
     * @return
     */
    @PostMapping("/new")
    public ResultEntity newUser(@RequestBody @Valid User user) {
        boolean isExist = mongoTemplate.query(User.class).matching(Query.query(where("email").exists(true).in(user.getEmail()).and("isSecession").ne(true))).exists();

        if (!isExist) {
            user.setId(sequenceGeneratorService.generateSequence(User.SEQUENCE_NAME));
            user.setPassword(BCrypt.hashpw(user.getPassword(), BCrypt.gensalt()));
            Date date = new Date();
            user.setCreateAt(date);
            user.setUpdateAt(date);

            return new ResultEntity(Constant.SUCCESS_CODE, mongoTemplate.insert(user));
        } else {
            throw new UserEmailExistException(user.getEmail());
        }
    }

    /**
     * 유저 탈퇴
     * @param id
     * @return
     */
    @PutMapping("/secession/{id}")
    public ResultEntity secessionUser(@PathVariable Long id) {
        Query query = new Query(where("isSecession").ne(true).and("_id").is(id));

        User user = mongoTemplate.findOne(query, User.class);
        if (Objects.isNull(user)) {
            throw new UserNotFoundException();
        }

        Update update = new Update();
        update.set("updateAt", new Date());
        update.set("isSecession", true);

        UpdateResult updateResult = mongoTemplate.updateFirst(query, update, User.class);

        return new ResultEntity(updateResult.wasAcknowledged() ? Constant.SUCCESS_CODE : Constant.FAILURE_CODE);
    }

}
