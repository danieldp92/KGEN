resultPath <- "C://Users//20190482//Documents//GitHub//Anonymization-metrics-implemetation-for-de-identification-problem//results//"

numberOfExperimentation <- 20

resultsKLEV <- list()
resultsKLOG <- list()

for (i in 1:numberOfExperimentation) {
  print(i)
  
  resultMOPath <- paste(resultPath, "result", sep = "")
  resultMOPath <- paste(resultMOPath, i, sep = "")
  resultMOPath <- paste(resultMOPath, ".csv", sep = "")
  resultMO1 <- read.table(resultMOPath, header = TRUE, sep = ";")
  
  resultsKLOG[[i]] <- resultMO1[, 5]
  resultsKLEV[[i]] <- resultMO1[, 6]
}

for (i in 1:numberOfExperimentation) {
  path <- paste(resultPath, "plot", sep = "")
  path <- paste(path, i, sep = "")
  path <- paste(path, ".png", sep = "")
  
  png(filename = path)
  
  plot(resultsKLOG[[i]], resultsKLEV[[i]], log="y")
  
  dev.off()
}
