package com.scmspain.repository;

import com.scmspain.entities.Tweet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TweetRepository extends JpaRepository<Tweet, String> {

  List<Tweet> findByDiscardedIsNotNullOrderByDiscardedDesc();

  Tweet findOneById(Long tweetId);

  List<Tweet> findAll();

}
