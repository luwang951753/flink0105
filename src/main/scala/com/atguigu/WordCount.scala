package com.atguigu

// 导入一些隐式类型转换，implicit
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.api.windowing.time.Time

object WordCount {

  case class WordWithCount(word: String, count: Int)

  def main(args: Array[String]): Unit = {
    // 获取运行时环境，类似SparkContext
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    // 设置分区（又叫并行任务）的数量为1
    env.setParallelism(1)

    // 建立数据源
    // 需要先启动`nc -lk 9999`，用来发送数据
    val stream = env.socketTextStream("hadoop108", 9999, '\n')

    // 写对流的转换处理逻辑
    val transformed = stream
      // 使用空格切分输入的字符串
      .flatMap(line => line.split("\\s"))
      // 类似MR中的map
      .map(w => WordWithCount(w, 1))
      // 使用word字段进行分组，shuffle
      .keyBy(0)
      // 开了一个5s钟的滚动窗口
      .timeWindow(Time.seconds(5))
      // 针对count字段进行累加操作，类似MR中的reduce
      .sum(1)

    // 将计算的结果输出到标准输出
    transformed.print()

    // 执行计算逻辑
    env.execute()
  }
}
