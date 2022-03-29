package com.nlxr.geektime.multi;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 多线演示
 *
 * @author labu
 * @date 2022/03/30
 */
public class MultiThreadDemoTest {

    @Test
    public void testCompletionServiceMin() {
        ExecutorService executor = Executors.newFixedThreadPool(3);
        ExecutorCompletionService<Integer> completionService = new ExecutorCompletionService<>(executor, new LinkedBlockingQueue<>(1000));
        //CompletionService.submit 返回封装了结果的future对象，并把执行的结果 future对象加入到阻塞队列中
        completionService.submit(this::geocoderByS1);
        completionService.submit(this::geocoderByS2);
        completionService.submit(this::geocoderByS3);
        AtomicReference<Integer> min = new AtomicReference<>(Integer.MAX_VALUE);
        for (int i = 0; i < 3; i++) {
//            executor.execute(()->{
                Integer r = null;
                try {
                    r = completionService.take().get();
                }catch (Exception ignored){}
                System.out.println(Thread.currentThread().getName()+" res = "+r);
                min.set(Integer.min(min.get(),r));
//            });
        }
        //take阻塞并非main线程，所以main线程必须sleep等到take到结果后才能正常取到最小值
        //或者不用线程池get结果(main线程不需要sleep)
        threadSleep(30);
        System.out.println("=======================result " + min.get());
    }

    @Test
    public void testCompletionService() {
        ExecutorService executor = Executors.newFixedThreadPool(3);
        ExecutorCompletionService<Integer> completionService = new ExecutorCompletionService<>(executor, new LinkedBlockingQueue<>(1000));
        List<Future<Integer>> futures = new ArrayList<>();
        //CompletionService.submit 返回封装了结果的future对象，并把执行的结果 future对象加入到阻塞队列中
        futures.add(completionService.submit(this::geocoderByS1));
        futures.add(completionService.submit(this::geocoderByS2));
        futures.add(completionService.submit(this::geocoderByS3));
        Integer r = 0;
        try {
            for (int i = 0; i < 3; i++) {
                System.out.println("------" + i);
                //从CompletionService中未获取到结果时，take会阻塞当前线程(调用take的线程)
                r = completionService.take().get();
                System.out.println("+++++++++++" + i);
                if (null != r) {
                    break;
                }
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        } finally {
            for (Future<Integer> future : futures) {
                future.cancel(true);
            }
        }
        System.out.println("=======================result " + r);
    }

    public Integer geocoderByS1() {
        threadSleep(10);
        System.out.println("===S1========");
        return 1;
    }

    public Integer geocoderByS2() {
        threadSleep(20);
        System.out.println("===S2========");
        return 2;
    }

    public Integer geocoderByS3() {
        threadSleep(30);
        System.out.println("===S3========");
        return 3;
    }

    private void threadSleep(long i) {
        try {
            TimeUnit.SECONDS.sleep(i);
        } catch (InterruptedException ignored) {
        }
    }
}
