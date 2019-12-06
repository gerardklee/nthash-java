# nthash-java
This program is used to search for all possible k-mer substrings in a really long DNA sequence using multi-threading and Rabin-Karp algorithm.

## Description
Genomic information is important for analyses of species and diseases. If there are many specific k-mers in a genome, that might mean something; it might be the emergence of a new gene or it might denote a certain disease. Unfortunately, there is no Java version of this program for such analyses. So, this program--as the name of the program says--efficiently searches all possible k-mers in a DNA sequence in Java.

## How to use the program
1. This program is released to the Central Repository and can be found on search.maven.org.
2. On search.maven.org, type in "nthash-java", click on the version, and the jar file can be downloaded.
3. Once downloaded, one can configure the buildpath and add this JAR file on JAVA IDE such as Eclipse.

## Build with
1. [Maven](https://maven.apache.org/) - Dependency Management <br />
2. [H2](https://www.h2database.com/html/main.html) - Database Engine <br />
3. [JUnit](https://junit.org/junit5/) - Testing Framework <br />
4. [Sonatype](https://www.sonatype.com/) - Build and Manage Artifacts <br />

## References
1. [ntHash: recursive nucleotide hashing](https://academic.oup.com/bioinformatics/article/32/22/3492/2525588) - Provided bit operation idea <br />
2. [Unique Seeds/Values](https://github.com/bcgsc/ntHash) - Provided unique values for each nucleotide A, G, T, and C <br />
