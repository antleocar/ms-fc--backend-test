package com.scmspain.services;

import com.scmspain.entities.Tweet;
import com.scmspain.repository.TweetRepository;
import org.springframework.boot.actuate.metrics.writer.Delta;
import org.springframework.boot.actuate.metrics.writer.MetricWriter;
import org.springframework.stereotype.Service;
import javax.transaction.Transactional;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@Transactional
public class TweetService {

    private static final String URL_PATTERN = "(https{0,1}:\\/\\/\\S*\\s)";
    private static final Pattern PATTERN = Pattern.compile(URL_PATTERN, Pattern.CASE_INSENSITIVE);

    private TweetRepository tweetRepository;
    private MetricWriter metricWriter;

    public TweetService(MetricWriter metricWriter, TweetRepository tweetRepository) {
        this.metricWriter = metricWriter;
        this.tweetRepository = tweetRepository;
    }

    /**
      Push tweet to repository
      Parameter - publisher - creator of the Tweet
      Parameter - text - Content of the Tweet
      Result - recovered Tweet
    */
    public void publishTweet(String publisher, String text) {
        if (validTweet(publisher, text)) {
            Tweet tweet = new Tweet();
            tweet.setTweet(text);
            tweet.setPublisher(publisher);

            this.metricWriter.increment(new Delta<Number>("published-tweets", 1));
            this.tweetRepository.save(tweet);
        } else {
            throw new IllegalArgumentException("Tweet must not be greater than 140 characters");
        }
    }

    /**
     Recover all tweets from repository.
     This is less efficient than the older but I want to use java streams.
     To perform this, should do a custom criteria for this query.
    */
    public List<Tweet> listAllTweets() {
        this.metricWriter.increment(new Delta<Number>("times-queried-tweets", 1));
        List<Tweet> tweets = tweetRepository.findAll();
        return tweets.stream()
            .filter(tweet -> tweet.getPre2015MigrationStatus() != 99 && tweet.getDiscarded() == null)
            .sorted((t1, t2) -> Long.compare(t2.getId(), t1.getId()))
            .collect(Collectors.toList());
    }

    public void discardTweet(Long tweetId) {
        Tweet tweet = tweetRepository.findOneById(tweetId);
        if (tweet != null) {
            tweet.setDiscarded(System.currentTimeMillis());
            this.metricWriter.increment(new Delta<Number>("discarded-tweets", 1));
            this.tweetRepository.save(tweet);
        } else {
            throw new IllegalArgumentException("Tweet not found");
        }
    }

    public List<Tweet> discarded() {
        this.metricWriter.increment(new Delta<Number>("times-queried-discarded-tweets", 1));
        return tweetRepository.findByDiscardedIsNotNullOrderByDiscardedDesc();
    }

    private String cleanedText(String text) {
        Matcher m = PATTERN.matcher(text);
        text = m.replaceAll("");
        return text;
    }

    private boolean validTweet(String publisher, String text) {
        String cleanedText = cleanedText(text);
        return publisher != null && publisher.length() > 0
               && cleanedText != null && cleanedText.length() > 0
               && cleanedText.length() < 140;
    }

}
