package com.Elessar.app.util;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.LongWritable;
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
    public static class MetricMapper extends Mapper<Object, Text, Text, LongWritable> {
        private Text operation = new Text();
        private LongWritable duration = new LongWritable();

        public void map(Object key, Text value, Context context)
                throws IOException, InterruptedException {

            String[] line = value.toString().split(",");
            operation.set(line[0]);
            duration.set(Long.valueOf(line[2]));
            context.write(operation, duration);
        }
    }

    public static class MetricAvgReducer extends Reducer<Text, LongWritable, Text, DoubleWritable> {
        private DoubleWritable durationAvg = new DoubleWritable();

        public void reduce(Text operation, Iterable<LongWritable> durations, Context context)
                throws IOException, InterruptedException {

            long count = 0;
            long sum = 0;
            for (LongWritable duration : durations) {
                sum += duration.get();
                count += 1;
            }

            durationAvg.set(sum * 1.0 / count);
            context.write(operation, durationAvg);
        }
    }

    public static class MetricCountReducer extends Reducer<Text, LongWritable, Text, LongWritable> {
        private LongWritable durationCount = new LongWritable();

        public void reduce(Text operation, Iterable<LongWritable> durations, Context context)
                throws IOException, InterruptedException {

            long count = 0;
            for (LongWritable duration : durations) {
                count += 1;
            }

            durationCount.set(count);
            context.write(operation, durationCount);
        }
    }

    public static void main(String[] args) throws Exception {
        Configuration conf = new Configuration();
        Job job = Job.getInstance(conf, "metric count");
        job.setJarByClass(MetricAnalyzer.class);
        job.setMapperClass(MetricMapper.class);
        job.setReducerClass(MetricAvgReducer.class);
//        job.setCombinerClass(MetricAvgReducer.class);

        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(LongWritable.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(DoubleWritable.class);
        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));
        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }
}
