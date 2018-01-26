import java.io.IOException;
import java.util.StringTokenizer;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class Wordpairs {

  public static class TokenizerMapper
       extends Mapper<Object, Text, Text, IntWritable>{
    // private WordPair wordPair = new WordPair();
    private final static IntWritable one = new IntWritable(1);
    private Text word = new Text();

    public void map(Object key, Text value, Context context
                    ) throws IOException, InterruptedException {
    	//String 
    	//int neighbors = context.getConfiguration().getInt("neighbors",2)
    	int start = 0;
      int end = 0;
      String[] tweets = value.toString().split("\\n+");
      for (int i1 = 0; i1 < tweets.length; i1++){
        String[] tokens = tweets[i1].toString().split("\\s+");
        if(tokens.length > 1) {
          for(int i=0; i < tokens.length; i++){
            if (i-1 < 0){
              start = 0;
            } else {
              start = i - 1;
            }
            if (i + 1 >= tokens.length) {
              end = tokens.length - 1; 
            } else {
              end = i + 1;
            }
            for (int j = start; j <= end; j++){
              if (j==i) continue;
              word.set(tokens[i]+","+tokens[j]);
              context.write(word,one);
            }
          }
        }

      }
      }
    }

  public static class IntSumReducer
       extends Reducer<Text,IntWritable,Text,IntWritable> {
    private IntWritable result = new IntWritable();

    public void reduce(Text key, Iterable<IntWritable> values,
                       Context context
                       ) throws IOException, InterruptedException {
      int sum = 0;
      for (IntWritable val : values) {
        sum += val.get();
      }
      result.set(sum);
      context.write(key, result);
    }
  }

  public static void main(String[] args) throws Exception {
    Configuration conf = new Configuration();
    Job job = Job.getInstance(conf, "WordPairs");
    job.setJarByClass(Wordpairs.class);
    job.setMapperClass(TokenizerMapper.class);
    job.setCombinerClass(IntSumReducer.class);
    job.setReducerClass(IntSumReducer.class);
    job.setOutputKeyClass(Text.class);
    job.setOutputValueClass(IntWritable.class);
    FileInputFormat.addInputPath(job, new Path(args[0]));
    FileOutputFormat.setOutputPath(job, new Path(args[1]));
    System.exit(job.waitForCompletion(true) ? 0 : 1);
  }
}
