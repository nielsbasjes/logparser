#!/usr/bin/env bash
mvn clean -PReportCoverage cobertura:cobertura
#mvn clean -PReportCoverage cobertura:cobertura coveralls:report -DrepoToken=x9qzn9KUXY9jYwcXBf1myIN6GLn8qwI5s
