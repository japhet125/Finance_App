library(tidyverse)
library(dplyr)
library(readr)
library(janitor)


get_url <- "https://raw.githubusercontent.com/japhet125/Multi_Agency_Violator/refs/heads/main/multi_agency_violators.csv" 

get_url_data <- read_csv(get_url)

get_url_data

clean_data <- get_url_data |>
  clean_names()
clean_data

