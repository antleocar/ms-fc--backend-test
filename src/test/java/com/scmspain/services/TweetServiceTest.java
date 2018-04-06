package com.scmspain.services;

import com.scmspain.entities.Tweet;
import com.scmspain.repository.TweetRepository;
import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.actuate.metrics.writer.MetricWriter;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TweetServiceTest {
    private MetricWriter metricWriter;
    private TweetService tweetService;
    private TweetRepository tweetRepository;

    private Tweet tweet;

    @Before
    public void setUp() throws Exception {
        this.tweetRepository = mock(TweetRepository.class);
        this.metricWriter = mock(MetricWriter.class);

        this.tweetService = new TweetService(metricWriter, tweetRepository);
        this.tweet = new Tweet();
        tweet.setPublisher("Publisher");
        tweet.setId(1L);
        tweet.setTweet("Content");
    }

    @Test
    public void shouldInsertANewTweet() throws Exception {
        tweetService.publishTweet("Guybrush Threepwood", "I am Guybrush Threepwood, mighty pirate.");

        verify(tweetRepository).save(any(Tweet.class));
    }

    @Test
    public void shouldUpdateTweetWhenIsDiscarded() {
        when(tweetRepository.findOneById(1L)).thenReturn(tweet);
        tweetService.discardTweet(1L);
        verify(tweetRepository).save(any(Tweet.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowAnExceptionWhenTweetLengthIsInvalid() throws Exception {
        tweetService.publishTweet("Pirate", "LeChuck? He's the guy that went to the Governor's for dinner and never wanted to leave. He fell for her in a big way, but she told him to drop dead. So he did. Then things really got ugly.");
    }

    @Test
    public void shouldPublishTweetWithCleanUrlLength() {
        tweetService.publishTweet("Pirate", "Oh no Pirate. You're not authorize. Let's try again. You should visit http://www.schibsted.es/ or http://www.coches.net/ for more information");
    }
}
