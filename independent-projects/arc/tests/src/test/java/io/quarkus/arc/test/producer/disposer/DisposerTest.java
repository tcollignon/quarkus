package io.quarkus.arc.test.producer.disposer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import io.quarkus.arc.Arc;
import io.quarkus.arc.InstanceHandle;
import io.quarkus.arc.test.ArcTestContainer;
import io.quarkus.arc.test.MyQualifier;
import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Produces;
import javax.enterprise.util.TypeLiteral;
import javax.inject.Singleton;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

public class DisposerTest {

    @Rule
    public RuleChain chain = RuleChain.outerRule(new TestRule() {
        @Override
        public Statement apply(final Statement base, Description description) {
            return new Statement() {

                @Override
                public void evaluate() throws Throwable {
                    base.evaluate();
                    assertNotNull(BigDecimalProducer.DISPOSED.get());
                    assertEquals(1, BigDecimalProducer.DESTROYED.get());
                }
            };
        }
    }).around(new ArcTestContainer(StringProducer.class, LongProducer.class, BigDecimalProducer.class, MyQualifier.class));

    @Test
    public void testDisposers() {
        InstanceHandle<Long> longHandle = Arc.container().instance(Long.class);
        Long longValue = longHandle.get();
        longHandle.close();
        assertEquals(LongProducer.DISPOSED.get(), longValue);
        // String is only injected in Long disposer
        assertNotNull(StringProducer.DISPOSED.get());
        // A new instance is created for produce and dispose
        assertEquals(2, StringProducer.DESTROYED.get());
        // Both producer and produced bean are application scoped
        @SuppressWarnings("serial")
        Comparable<BigDecimal> bigDecimal = Arc.container().instance(new TypeLiteral<Comparable<BigDecimal>>() {
        }).get();
        assertEquals(0, bigDecimal.compareTo(BigDecimal.ONE));
    }

    @Singleton
    static class LongProducer {

        static final AtomicReference<Long> DISPOSED = new AtomicReference<>();

        @Dependent
        @Produces
        Long produce() {
            return System.currentTimeMillis();
        }

        void dipose(@Disposes Long value, @MyQualifier String injectedString) {
            assertNotNull(injectedString);
            DISPOSED.set(value);
        }

    }

    @Dependent
    static class StringProducer {

        static final AtomicInteger DESTROYED = new AtomicInteger();

        static final AtomicReference<String> DISPOSED = new AtomicReference<>();

        @MyQualifier
        @Produces
        String produce = toString();

        void dipose(@Disposes @MyQualifier String value) {
            DISPOSED.set(value);
        }

        @PreDestroy
        void destroy() {
            DESTROYED.incrementAndGet();
        }

    }

    @ApplicationScoped
    static class BigDecimalProducer {

        static final AtomicInteger DESTROYED = new AtomicInteger();

        static final AtomicReference<Object> DISPOSED = new AtomicReference<>();

        @ApplicationScoped
        @Produces
        Comparable<BigDecimal> produce() {
            return BigDecimal.ONE;
        }

        void dipose(@Disposes Comparable<BigDecimal> value) {
            DISPOSED.set(value);
        }

        @PreDestroy
        void destroy() {
            DESTROYED.incrementAndGet();
        }

    }

}
