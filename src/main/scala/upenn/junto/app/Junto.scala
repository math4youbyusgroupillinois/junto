package upenn.junto.app

/**
 * Copyright 2011 Partha Talukudar, Jason Baldridge
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.util.{ArrayList,Hashtable}
import gnu.trove.TObjectDoubleHashMap
import upenn.junto.algorithm._
import upenn.junto.config._
import upenn.junto.graph._
import upenn.junto.util._

object JuntoRunner {

  def apply (algo: String, graph: Graph, maxIters: Int, 
             mu1: Double, mu2: Double, mu3: Double,
             keepTopKLabels: Int, useBipartiteOptimization: Boolean,
             verbose: Boolean, resultList: ArrayList[TObjectDoubleHashMap[String]]) {

    algo match {
      case "adsorption" => {
        MessagePrinter.Print("Using " + algo + " ...\n")
        Adsorption.Run(graph, maxIters, "original", mu1, mu2, mu3, keepTopKLabels, 
                       useBipartiteOptimization, verbose, resultList)
      }
      case "mad" => {
        MessagePrinter.Print("Using " + algo + " ...\n")
        Adsorption.Run(graph, maxIters, "modified", mu1, mu2, mu3, keepTopKLabels, 
                       useBipartiteOptimization, verbose, resultList)
      }
      case "lp_zgl" => {
        MessagePrinter.Print("Using Label Propagation (ZGL) ...\n")
        LP_ZGL.Run(graph, maxIters, mu2, keepTopKLabels, 
                   useBipartiteOptimization, verbose, resultList)
      }
      case _ => MessagePrinter.PrintAndDie("Unknown algorithm: " + algo)
    }

  }

}

object JuntoConfigRunner {

  def apply (config: Hashtable[String,String], 
             resultList: ArrayList[TObjectDoubleHashMap[String]]) = {

    // pretty print the configs
    println(CollectionUtil.Map2StringPrettyPrint(config))
		
    // load the graph
    val graph = GraphConfigLoader(config)

    val maxIters = Integer.parseInt(config.get("iters"))

    var verbose = true
    if (config.containsKey("verbose"))
      verbose = config.get("verbose").toBoolean

    val mu1 = Defaults.GetValueOrDefault(config.get("mu1"), 1.0)
    val mu2 = Defaults.GetValueOrDefault(config.get("mu2"), 1.0)
    val mu3 = Defaults.GetValueOrDefault(config.get("mu3"), 1.0)
    val keepTopKLabels =
      Defaults.GetValueOrDefault(config.get("keep_top_k_labels"), Integer.MAX_VALUE)
    MessagePrinter.Print("Using keep_top_k_labels value: " + keepTopKLabels)
		
    // this flag should be set to false (the default), unless you really
    // know what you are doing
    val useBipartiteOptimization =
      Defaults.GetValueOrDefault(config.get("use_bipartite_optimization"), false)
		
    // decide on the algorithm to use
    val algo = Defaults.GetValueOrDefault(config.get("algo"), "adsorption")

    JuntoRunner(algo, graph, maxIters, mu1, mu2, mu3, keepTopKLabels,
                useBipartiteOptimization, verbose, resultList)
		
    if (config.containsKey("output_file") && (config.get("output_file")).length > 0)
      graph.SaveEstimatedScores(config.get("output_file"))
    else if (config.containsKey("output_base") && (config.get("output_base")).length > 0)
      graph.SaveEstimatedScores(config.get("output_base") + ".mu2_" + mu2 + ".mu3_" + mu3)

  }

  def main (args: Array[String]) = 
    apply(ConfigReader.read_config(args), new ArrayList[TObjectDoubleHashMap[String]])

}
