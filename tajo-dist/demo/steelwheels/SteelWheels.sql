CREATE EXTERNAL TABLE `customer_w_ter` (
  `CUSTOMERNUMBER` int8,
  `CUSTOMERNAME` text ,
  `CONTACTLASTNAME` text ,
  `CONTACTFIRSTNAME` text ,
  `PHONE` text ,
  `ADDRESSLINE1` text ,
  `ADDRESSLINE2` text ,
  `CITY` text ,
  `STATE` text ,
  `POSTALCODE` text ,
  `COUNTRY` text ,
  `EMPLOYEENUMBER` int8,
  `CREDITLIMIT` float8,
  `TERRITORY` text
) USING CSV WITH ('csvfile.delimiter'='|') LOCATION 'file:TAJO_HOME/demo/steelwheels/customer_w_ter';
CREATE EXTERNAL TABLE `customers` (
  `CUSTOMERNUMBER` int8,
  `CUSTOMERNAME` text ,
  `CONTACTLASTNAME` text ,
  `CONTACTFIRSTNAME` text ,
  `PHONE` text ,
  `ADDRESSLINE1` text ,
  `ADDRESSLINE2` text ,
  `CITY` text ,
  `STATE` text ,
  `POSTALCODE` text ,
  `COUNTRY` text ,
  `SALESREPEMPLOYEENUMBER` int8,
  `CREDITLIMIT` float8
) USING CSV WITH ('csvfile.delimiter'='|') LOCATION 'file:TAJO_HOME/demo/steelwheels/customers';
CREATE EXTERNAL TABLE `department_managers` (
  `REGION` text ,
  `MANAGER_NAME` text ,
  `EMAIL` text 
) USING CSV WITH ('csvfile.delimiter'='|') LOCATION 'file:TAJO_HOME/demo/steelwheels/department_managers';
CREATE EXTERNAL TABLE `employees` (
  `EMPLOYEENUMBER` int8,
  `LASTNAME` text ,
  `FIRSTNAME` text ,
  `EXTENSION` text ,
  `EMAIL` text ,
  `OFFICECODE` text ,
  `REPORTSTO` int8,
  `JOBTITLE` text
) USING CSV WITH ('csvfile.delimiter'='|') LOCATION 'file:TAJO_HOME/demo/steelwheels/employees';
CREATE EXTERNAL TABLE `offices` (
  `OFFICECODE` text ,
  `CITY` text ,
  `PHONE` text ,
  `ADDRESSLINE1` text ,
  `ADDRESSLINE2` text ,
  `STATE` text ,
  `COUNTRY` text ,
  `POSTALCODE` text ,
  `TERRITORY` text 
) USING CSV WITH ('csvfile.delimiter'='|') LOCATION 'file:TAJO_HOME/demo/steelwheels/offices';
CREATE EXTERNAL TABLE `orderdetails` (
  `ORDERNUMBER` int8,
  `PRODUCTCODE` text ,
  `QUANTITYORDERED` int8,
  `PRICEEACH` float8,
  `ORDERLINENUMBER` int2
) USING CSV WITH ('csvfile.delimiter'='|') LOCATION 'file:TAJO_HOME/demo/steelwheels/orderdetails';
CREATE EXTERNAL TABLE `orderfact` (
  `ORDERNUMBER` int8,
  `PRODUCTCODE` text ,
  `QUANTITYORDERED` int8,
  `PRICEEACH` float8,
  `ORDERLINENUMBER` int8,
  `TOTALPRICE` double,
  `ORDERDATE` timestamp,
  `REQUIREDDATE` timestamp,
  `SHIPPEDDATE` timestamp,
  `STATUS` text ,
  `COMMENTS` text ,
  `CUSTOMERNUMBER` int8,
  `TIME_ID` text ,
  `QTR_ID` bigint,
  `MONTH_ID` bigint,
  `YEAR_ID` bigint
) USING CSV WITH ('csvfile.delimiter'='|') LOCATION 'file:TAJO_HOME/demo/steelwheels/orderfact';
CREATE EXTERNAL TABLE `orders` (
  `ORDERNUMBER` int8,
  `ORDERDATE` timestamp,
  `REQUIREDDATE` timestamp,
  `SHIPPEDDATE` timestamp,
  `STATUS` text ,
  `COMMENTS` text ,
  `CUSTOMERNUMBER` int8
) USING CSV WITH ('csvfile.delimiter'='|') LOCATION 'file:TAJO_HOME/demo/steelwheels/orders';
CREATE EXTERNAL TABLE `payments` (
  `CUSTOMERNUMBER` int8,
  `CHECKNUMBER` text ,
  `PAYMENTDATE` timestamp,
  `AMOUNT` float8
) USING CSV WITH ('csvfile.delimiter'='|') LOCATION 'file:TAJO_HOME/demo/steelwheels/payments';
CREATE EXTERNAL TABLE `products` (
  `PRODUCTCODE` text ,
  `PRODUCTNAME` text ,
  `PRODUCTLINE` text ,
  `PRODUCTSCALE` text ,
  `PRODUCTVENDOR` text ,
  `PRODUCTDESCRIPTION` text ,
  `QUANTITYINSTOCK` int2,
  `BUYPRICE` float8,
  `MSRP` float8
) USING CSV WITH ('csvfile.delimiter'='|') LOCATION 'file:TAJO_HOME/demo/steelwheels/products';
CREATE EXTERNAL TABLE `quadrant_actuals` (
  `REGION` text ,
  `DEPARTMENT` text ,
  `POSITIONTITLE` text ,
  `ACTUAL` float8,
  `BUDGET` float8,
  `VARIANCE` float8
) USING CSV WITH ('csvfile.delimiter'='|') LOCATION 'file:TAJO_HOME/demo/steelwheels/quadrant_actuals';
CREATE EXTERNAL TABLE `dim_time` (
  `TIME_ID` text ,
  `MONTH_ID` int8,
  `QTR_ID` int8,
  `YEAR_ID` int8,
  `MONTH_NAME` text,
  `MONTH_DESC` text ,
  `QTR_NAME` text ,
  `QTR_DESC` text 
) USING CSV WITH ('csvfile.delimiter'='|') LOCATION 'file:TAJO_HOME/demo/steelwheels/dim_time';
CREATE EXTERNAL TABLE `trial_balance` (
  `Type` text,
  `Account_Num` int8,
  `Category` text ,
  `Category2` text ,
  `Detail` text ,
  `Amount` int8
) USING CSV WITH ('csvfile.delimiter'='|') LOCATION 'file:TAJO_HOME/demo/steelwheels/trial_balance';
