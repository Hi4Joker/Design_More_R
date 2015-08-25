package com.futurice.project.test.robolectric;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import org.mockito.InOrder;
import com.futurice.project.Solution;
import com.futurice.project.test.robolectric.RobolectricGradleTestRunner;
import java.util.concurrent.TimeUnit;
import rx.Observer;
import rx.schedulers.TestScheduler;
import rx.subjects.PublishSubject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

@Config(emulateSdk = 18)
@RunWith(RobolectricGradleTestRunner.class)
public class SolutionTest {

    @Test
    public void test_solutionOperatorFindsSimpleSecretSequence() throws Exception {
        @SuppressWarnings("unchecked")
        Observer<String> observer = mock(Observer.class);
        TestScheduler s = new TestScheduler();
        PublishSubject<String> o = PublishSubject.create();
        Solution.defineSuccessStream(o, s).subscribe(observer);

        // send events with simulated time increments
        s.advanceTimeTo(0, TimeUnit.MILLISECONDS);
        o.onNext("A");
        s.advanceTimeTo(50, TimeUnit.MILLISECONDS);
        o.onNext("B");
        s.advanceTimeTo(100, TimeUnit.MILLISECONDS);
        o.onNext("B");
        s.advanceTimeTo(150, TimeUnit.MILLISECONDS);
        o.onNext("A");
        s.advanceTimeTo(200, TimeUnit.MILLISECONDS);
        o.onNext("B");
        s.advanceTimeTo(250, TimeUnit.MILLISECONDS);
        o.onNext("A");

        InOrder inOrder = inOrder(observer);
        inOrder.verify(observer).onNext("ABBABA");
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void test_solutionOperatorIgnoresSecretSequenceIfNotWithinTimeout() throws Exception {
        @SuppressWarnings("unchecked")
        Observer<String> observer = mock(Observer.class);
        TestScheduler s = new TestScheduler();
        PublishSubject<String> o = PublishSubject.create();
        Solution.defineSuccessStream(o, s).subscribe(observer);

        // send events with simulated time increments
        s.advanceTimeTo(0, TimeUnit.MILLISECONDS);
        o.onNext("A");
        s.advanceTimeTo(50, TimeUnit.MILLISECONDS);
        o.onNext("B");
        s.advanceTimeTo(100, TimeUnit.MILLISECONDS);
        o.onNext("B");
        s.advanceTimeTo(150, TimeUnit.MILLISECONDS);
        o.onNext("A");
        s.advanceTimeTo(200, TimeUnit.MILLISECONDS);
        o.onNext("B");
        s.advanceTimeTo(8000, TimeUnit.MILLISECONDS);
        o.onNext("A");

        InOrder inOrder = inOrder(observer);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void test_solutionOperatorIgnoresWrongSequences() throws Exception {
        @SuppressWarnings("unchecked")
        Observer<String> observer = mock(Observer.class);
        TestScheduler s = new TestScheduler();
        PublishSubject<String> o = PublishSubject.create();
        Solution.defineSuccessStream(o, s).subscribe(observer);

        // send events with simulated time increments
        s.advanceTimeTo(0, TimeUnit.MILLISECONDS);
        o.onNext("A");
        s.advanceTimeTo(50, TimeUnit.MILLISECONDS);
        o.onNext("B");
        s.advanceTimeTo(100, TimeUnit.MILLISECONDS);
        o.onNext("B");
        s.advanceTimeTo(150, TimeUnit.MILLISECONDS);
        o.onNext("A");
        s.advanceTimeTo(200, TimeUnit.MILLISECONDS);
        o.onNext("B");
        s.advanceTimeTo(250, TimeUnit.MILLISECONDS);
        o.onNext("B");
        s.advanceTimeTo(300, TimeUnit.MILLISECONDS);
        o.onNext("A");
        s.advanceTimeTo(350, TimeUnit.MILLISECONDS);
        o.onNext("B");
        s.advanceTimeTo(400, TimeUnit.MILLISECONDS);
        o.onNext("B");
        s.advanceTimeTo(450, TimeUnit.MILLISECONDS);
        o.onNext("A");
        s.advanceTimeTo(500, TimeUnit.MILLISECONDS);
        o.onNext("B");
        s.advanceTimeTo(550, TimeUnit.MILLISECONDS);
        o.onNext("B");

        InOrder inOrder = inOrder(observer);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void test_solutionOperatorFindsSecretSequenceAsASuffix() throws Exception {
        @SuppressWarnings("unchecked")
        Observer<String> observer = mock(Observer.class);
        TestScheduler s = new TestScheduler();
        PublishSubject<String> o = PublishSubject.create();
        Solution.defineSuccessStream(o, s).subscribe(observer);

        // send events with simulated time increments
        s.advanceTimeTo(0, TimeUnit.MILLISECONDS);
        o.onNext("A");
        s.advanceTimeTo(50, TimeUnit.MILLISECONDS);
        o.onNext("B");
        s.advanceTimeTo(100, TimeUnit.MILLISECONDS);
        o.onNext("B");
        s.advanceTimeTo(150, TimeUnit.MILLISECONDS);
        o.onNext("A");
        s.advanceTimeTo(200, TimeUnit.MILLISECONDS);
        o.onNext("B");
        s.advanceTimeTo(250, TimeUnit.MILLISECONDS);
        o.onNext("B");
        s.advanceTimeTo(300, TimeUnit.MILLISECONDS);
        o.onNext("A");
        s.advanceTimeTo(350, TimeUnit.MILLISECONDS);
        o.onNext("B");
        s.advanceTimeTo(400, TimeUnit.MILLISECONDS);
        o.onNext("B");
        s.advanceTimeTo(450, TimeUnit.MILLISECONDS);
        o.onNext("A");
        s.advanceTimeTo(500, TimeUnit.MILLISECONDS);
        o.onNext("B");
        s.advanceTimeTo(550, TimeUnit.MILLISECONDS);
        o.onNext("A");

        InOrder inOrder = inOrder(observer);
        inOrder.verify(observer).onNext("ABBABA");
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void test_solutionOperatorIgnoresSecretSequenceIfNotWithinTimeoutOfFirstInput() throws Exception {
        @SuppressWarnings("unchecked")
        Observer<String> observer = mock(Observer.class);
        TestScheduler s = new TestScheduler();
        PublishSubject<String> o = PublishSubject.create();
        Solution.defineSuccessStream(o, s).subscribe(observer);

        // send events with simulated time increments
        s.advanceTimeTo(0, TimeUnit.MILLISECONDS);
        o.onNext("A");
        s.advanceTimeTo(100, TimeUnit.MILLISECONDS);
        o.onNext("B");
        s.advanceTimeTo(200, TimeUnit.MILLISECONDS);
        o.onNext("B");
        s.advanceTimeTo(300, TimeUnit.MILLISECONDS);
        o.onNext("A");
        s.advanceTimeTo(400, TimeUnit.MILLISECONDS);
        o.onNext("B");
        s.advanceTimeTo(4200, TimeUnit.MILLISECONDS);
        o.onNext("A");

        InOrder inOrder = inOrder(observer);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void test_solutionOperatorFindsSecretSequenceWithinTightTimeout() throws Exception {
        @SuppressWarnings("unchecked")
        Observer<String> observer = mock(Observer.class);
        TestScheduler s = new TestScheduler();
        PublishSubject<String> o = PublishSubject.create();
        Solution.defineSuccessStream(o, s).subscribe(observer);

        // send events with simulated time increments
        s.advanceTimeTo(0, TimeUnit.MILLISECONDS);
        o.onNext("A");
        s.advanceTimeTo(100, TimeUnit.MILLISECONDS);
        o.onNext("B");
        s.advanceTimeTo(200, TimeUnit.MILLISECONDS);
        o.onNext("B");
        s.advanceTimeTo(300, TimeUnit.MILLISECONDS);
        o.onNext("A");
        s.advanceTimeTo(400, TimeUnit.MILLISECONDS);
        o.onNext("B");
        s.advanceTimeTo(3900, TimeUnit.MILLISECONDS);
        o.onNext("A");

        InOrder inOrder = inOrder(observer);
        inOrder.verify(observer).onNext("ABBABA");
        inOrder.verifyNoMoreInteractions();
    }

}
