package com.Elessar.app.util;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

/**
 * Created by Hans on 3/7/19.
 */
public class MetricAnalyzer {
    public static class MetricMapper extends Mapper<Object, Text, Text, DoubleWritable> {
        private final Text operation = new Text();
        private final DoubleWritable duration = new DoubleWritable();

        public void map(Object key, Text value, Context context)
                throws IOException, InterruptedException {

            final String[] line = value.toString().split(",");
            operation.set(line[0]);
            duration.set(Double.valueOf(line[2]));
            context.write(operation, duration);
        }
    }

    public static class MetricBucketMapper extends Mapper<Object, Text, Text, DoubleWritable> {
        private final int bucketSize = 20;
        private final Text operation_bucketID = new Text();


        public void map(Object key, Text value, Context context)
                throws IOException, InterruptedException {

            final String[] line = value.toString().split(",");
            final int bucketID = Integer.parseInt(line[2]) / bucketSize;
            operation_bucketID.set(line[0]+ "_" + bucketID);
            context.write(operation_bucketID, new DoubleWritable(1));
        }
    }

    public static class MetricAvgReducer extends Reducer<Text, DoubleWritable, Text, DoubleWritable> {
        private final DoubleWritable durationAvg = new DoubleWritable();

        public void reduce(Text operation, Iterable<DoubleWritable> durations, Context context)
                throws IOException, InterruptedException {

            long count = 0;
            double sum = 0;
            for (DoubleWritable duration : durations) {
                sum += duration.get();
                count += 1;
            }

            durationAvg.set(sum * 1.0 / count);
            context.write(operation, durationAvg);
        }
    }

    public static class MetricCountReducer extends Reducer<Text, DoubleWritable, Text, DoubleWritable> {
        private final DoubleWritable durationCount = new DoubleWritable();

        public void reduce(Text operation, Iterable<DoubleWritable> durations, Context context)
                throws IOException, InterruptedException {

            long count = 0;
            for (DoubleWritable duration : durations) {
                count += 1;
            }

            durationCount.set(count);
            context.write(operation, durationCount);
        }
    }

    public static void main(String[] args) throws Exception {
        final Configuration conf = new Configuration();
        final Job job = Job.getInstance(conf, "metric count");
        job.setJarByClass(MetricAnalyzer.class);
        if ("histogram".equals(args[0])) {
            job.setMapperClass(MetricBucketMapper.class);
        } else {
            job.setMapperClass(MetricMapper.class);
        }

        if ("average".equals(args[0])) {
            job.setReducerClass(MetricAvgReducer.class);
//            job.setCombinerClass(MetricAvgReducer.class);
        } else {
            job.setReducerClass(MetricCountReducer.class);
//            job.setCombinerClass(MetricCountReducer.class);
        }

        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(DoubleWritable.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(DoubleWritable.class);
        FileInputFormat.addInputPath(job, new Path(args[1]));
        FileOutputFormat.setOutputPath(job, new Path(args[2]));
        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }
}