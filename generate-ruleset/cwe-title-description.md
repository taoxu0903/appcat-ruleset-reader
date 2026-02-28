# CWE Title and Description

## CWE-22: Improper Limitation of a Pathname to a Restricted Directory ('Path Traversal')

- URL: https://cwe.mitre.org/data/definitions/22.html
- Description: The product uses external input to construct a pathname that is intended to identify a file or directory that is located underneath a restricted parent directory, but the product does not properly neutralize special elements within the pathname that can cause the pathname to resolve to a location that is outside of the restricted directory.

## CWE-23: Relative Path Traversal

- URL: https://cwe.mitre.org/data/definitions/23.html
- Description: The product uses external input to construct a pathname that should be within a restricted directory, but it does not properly neutralize sequences such as ".." that can resolve to a location that is outside of that directory.

## CWE-36: Absolute Path Traversal

- URL: https://cwe.mitre.org/data/definitions/36.html
- Description: The product uses external input to construct a pathname that should be within a restricted directory, but it does not properly neutralize absolute path sequences such as "/abs/path" that can resolve to a location that is outside of that directory.

## CWE-77: Improper Neutralization of Special Elements used in a Command ('Command Injection')

- URL: https://cwe.mitre.org/data/definitions/77.html
- Description: The product constructs all or part of a command using externally-influenced input from an upstream component, but it does not neutralize or incorrectly neutralizes special elements that could modify the intended command when it is sent to a downstream component.

## CWE-78: Improper Neutralization of Special Elements used in an OS Command ('OS Command Injection')

- URL: https://cwe.mitre.org/data/definitions/78.html
- Description: The product constructs all or part of an OS command using externally-influenced input from an upstream component, but it does not neutralize or incorrectly neutralizes special elements that could modify the intended OS command when it is sent to a downstream component.

## CWE-88: Improper Neutralization of Argument Delimiters in a Command ('Argument Injection')

- URL: https://cwe.mitre.org/data/definitions/88.html
- Description: The product constructs a string for a command to be executed by a separate component in another control sphere, but it does not properly delimit the intended arguments, options, or switches within that command string.

## CWE-79: Improper Neutralization of Input During Web Page Generation ('Cross-site Scripting')

- URL: https://cwe.mitre.org/data/definitions/79.html
- Description: The product does not neutralize or incorrectly neutralizes user-controllable input before it is placed in output that is used as a web page that is served to other users.

## CWE-89: Improper Neutralization of Special Elements used in an SQL Command ('SQL Injection')

- URL: https://cwe.mitre.org/data/definitions/89.html
- Description: The product constructs all or part of an SQL command using externally-influenced input from an upstream component, but it does not neutralize or incorrectly neutralizes special elements that could modify the intended SQL command when it is sent to a downstream component. Without sufficient removal or quoting of SQL syntax in user-controllable inputs, the generated SQL query can cause those inputs to be interpreted as SQL instead of ordinary user data.

## CWE-564: SQL Injection: Hibernate

- URL: https://cwe.mitre.org/data/definitions/564.html
- Description: Using Hibernate to execute a dynamic SQL statement built with user-controlled input can allow an attacker to modify the statement's meaning or to execute arbitrary SQL commands.

## CWE-90: Improper Neutralization of Special Elements used in an LDAP Query ('LDAP Injection')

- URL: https://cwe.mitre.org/data/definitions/90.html
- Description: The product constructs all or part of an LDAP query using externally-influenced input from an upstream component, but it does not neutralize or incorrectly neutralizes special elements that could modify the intended LDAP query when it is sent to a downstream component.

## CWE-91: XML Injection (aka Blind XPath Injection)

- URL: https://cwe.mitre.org/data/definitions/91.html
- Description: The product does not properly neutralize special elements that are used in XML, allowing attackers to modify the syntax, content, or commands of the XML before it is processed by an end system.

## CWE-99: Improper Control of Resource Identifiers ('Resource Injection')

- URL: https://cwe.mitre.org/data/definitions/99.html
- Description: The product receives input from an upstream component, but it does not restrict or incorrectly restricts the input before it is used as an identifier for a resource that may be outside the intended sphere of control.

## CWE-119: Improper Restriction of Operations within the Bounds of a Memory Buffer

- URL: https://cwe.mitre.org/data/definitions/119.html
- Description: The product performs operations on a memory buffer, but it reads from or writes to a memory location outside the buffer's intended boundary. This may result in read or write operations on unexpected memory locations that could be linked to other variables, data structures, or internal program data.

## CWE-120: Buffer Copy without Checking Size of Input ('Classic Buffer Overflow')

- URL: https://cwe.mitre.org/data/definitions/120.html
- Description: The product copies an input buffer to an output buffer without verifying that the size of the input buffer is less than the size of the output buffer.

## CWE-123: Write-what-where Condition

- URL: https://cwe.mitre.org/data/definitions/123.html
- Description: Any condition where the attacker has the ability to write an arbitrary value to an arbitrary location, often as the result of a buffer overflow.

## CWE-125: Out-of-bounds Read

- URL: https://cwe.mitre.org/data/definitions/125.html
- Description: The product reads data past the end, or before the beginning, of the intended buffer.

## CWE-130: Improper Handling of Length Parameter Inconsistency

- URL: https://cwe.mitre.org/data/definitions/130.html
- Description: The product parses a formatted message or structure, but it does not handle or incorrectly handles a length field that is inconsistent with the actual length of the associated data.

## CWE-786: Access of Memory Location Before Start of Buffer

- URL: https://cwe.mitre.org/data/definitions/786.html
- Description: The product reads or writes to a buffer using an index or pointer that references a memory location prior to the beginning of the buffer.

## CWE-787: Out-of-bounds Write

- URL: https://cwe.mitre.org/data/definitions/787.html
- Description: The product writes data past the end, or before the beginning, of the intended buffer.

## CWE-788: Access of Memory Location After End of Buffer

- URL: https://cwe.mitre.org/data/definitions/788.html
- Description: The product reads or writes to a buffer using an index or pointer that references a memory location after the end of the buffer.

## CWE-805: Buffer Access with Incorrect Length Value

- URL: https://cwe.mitre.org/data/definitions/805.html
- Description: The product uses a sequential operation to read or write a buffer, but it uses an incorrect length value that causes it to access memory that is outside of the bounds of the buffer.

## CWE-822: Untrusted Pointer Dereference

- URL: https://cwe.mitre.org/data/definitions/822.html
- Description: The product obtains a value from an untrusted source, converts this value to a pointer, and dereferences the resulting pointer.

## CWE-823: Use of Out-of-range Pointer Offset

- URL: https://cwe.mitre.org/data/definitions/823.html
- Description: The product performs pointer arithmetic on a valid pointer, but it uses an offset that can point outside of the intended range of valid memory locations for the resulting pointer.

## CWE-824: Access of Uninitialized Pointer

- URL: https://cwe.mitre.org/data/definitions/824.html
- Description: The product accesses or uses a pointer that has not been initialized.

## CWE-825: Expired Pointer Dereference

- URL: https://cwe.mitre.org/data/definitions/825.html
- Description: The product dereferences a pointer that contains a location for memory that was previously valid, but is no longer valid.

## CWE-259: Use of Hard-coded Password

- URL: https://cwe.mitre.org/data/definitions/259.html
- Description: The product contains a hard-coded password, which it uses for its own inbound authentication or for outbound communication to external components.

## CWE-321: Use of Hard-coded Cryptographic Key

- URL: https://cwe.mitre.org/data/definitions/321.html
- Description: The product uses a hard-coded, unchangeable cryptographic key.

## CWE-434: Unrestricted Upload of File with Dangerous Type

- URL: https://cwe.mitre.org/data/definitions/434.html
- Description: The product allows the upload or transfer of dangerous file types that are automatically processed within its environment.

## CWE-456: Missing Initialization of a Variable

- URL: https://cwe.mitre.org/data/definitions/456.html
- Description: The product does not initialize critical variables, which causes the execution environment to use unexpected values.

## CWE-457: Use of Uninitialized Variable

- URL: https://cwe.mitre.org/data/definitions/457.html
- Description: The code uses a variable that has not been initialized, leading to unpredictable or unintended results.

## CWE-477: Use of Obsolete Function

- URL: https://cwe.mitre.org/data/definitions/477.html
- Description: The code uses deprecated or obsolete functions, which suggests that the code has not been actively reviewed or maintained.

## CWE-502: Deserialization of Untrusted Data

- URL: https://cwe.mitre.org/data/definitions/502.html
- Description: The product deserializes untrusted data without sufficiently ensuring that the resulting data will be valid.

## CWE-543: Use of Singleton Pattern Without Synchronization in a Multithreaded Context

- URL: https://cwe.mitre.org/data/definitions/543.html
- Description: The product uses the singleton pattern when creating a resource within a multithreaded environment.

## CWE-567: Unsynchronized Access to Shared Data in a Multithreaded Context

- URL: https://cwe.mitre.org/data/definitions/567.html
- Description: The product does not properly synchronize shared data, such as static variables across threads, which can lead to undefined behavior and unpredictable data changes.

## CWE-570: Expression is Always False

- URL: https://cwe.mitre.org/data/definitions/570.html
- Description: The product contains an expression that will always evaluate to false.

## CWE-571: Expression is Always True

- URL: https://cwe.mitre.org/data/definitions/571.html
- Description: The product contains an expression that will always evaluate to true.

## CWE-606: Unchecked Input for Loop Condition

- URL: https://cwe.mitre.org/data/definitions/606.html
- Description: The product does not properly check inputs that are used for loop conditions, potentially leading to a denial of service or other consequences because of excessive looping.

## CWE-643: Improper Neutralization of Data within XPath Expressions ('XPath Injection')

- URL: https://cwe.mitre.org/data/definitions/643.html
- Description: The product uses external input to dynamically construct an XPath expression used to retrieve data from an XML database, but it does not neutralize or incorrectly neutralizes that input. This allows an attacker to control the structure of the query.

## CWE-652: Improper Neutralization of Data within XQuery Expressions ('XQuery Injection')

- URL: https://cwe.mitre.org/data/definitions/652.html
- Description: The product uses external input to dynamically construct an XQuery expression used to retrieve data from an XML database, but it does not neutralize or incorrectly neutralizes that input. This allows an attacker to control the structure of the query.

## CWE-662: Improper Synchronization

- URL: https://cwe.mitre.org/data/definitions/662.html
- Description: The product utilizes multiple threads, processes, components, or systems to allow temporary access to a shared resource that can only be exclusive to one process at a time, but it does not properly synchronize these actions, which might cause simultaneous accesses of this resource by multiple threads or processes.

## CWE-665: Improper Initialization

- URL: https://cwe.mitre.org/data/definitions/665.html
- Description: The product does not initialize or incorrectly initializes a resource, which might leave the resource in an unexpected state when it is accessed or used.

## CWE-667: Improper Locking

- URL: https://cwe.mitre.org/data/definitions/667.html
- Description: The product does not properly acquire or release a lock on a resource, leading to unexpected resource state changes and behaviors.

## CWE-672: Operation on a Resource after Expiration or Release

- URL: https://cwe.mitre.org/data/definitions/672.html
- Description: The product uses, accesses, or otherwise operates on a resource after that resource has been expired, released, or revoked.

## CWE-681: Incorrect Conversion between Numeric Types

- URL: https://cwe.mitre.org/data/definitions/681.html
- Description: When converting from one data type to another, such as long to integer, data can be omitted or translated in a way that produces unexpected values. If the resulting values are used in a sensitive context, then dangerous behaviors may occur.

## CWE-682: Incorrect Calculation

- URL: https://cwe.mitre.org/data/definitions/682.html
- Description: The product performs a calculation that generates incorrect or unintended results that are later used in security-critical decisions or resource management.

## CWE-732: Incorrect Permission Assignment for Critical Resource

- URL: https://cwe.mitre.org/data/definitions/732.html
- Description: The product specifies permissions for a security-critical resource in a way that allows that resource to be read or modified by unintended actors.

## CWE-772: Missing Release of Resource after Effective Lifetime

- URL: https://cwe.mitre.org/data/definitions/772.html
- Description: The product does not release a resource after its effective lifetime has ended, i.e., after the resource is no longer needed.

## CWE-775: Missing Release of File Descriptor or Handle after Effective Lifetime

- URL: https://cwe.mitre.org/data/definitions/775.html
- Description: The product does not release a file descriptor or handle after its effective lifetime has ended, i.e., after the file descriptor/handle is no longer needed.

## CWE-778: Insufficient Logging

- URL: https://cwe.mitre.org/data/definitions/778.html
- Description: When a security-critical event occurs, the product either does not record the event or omits important details about the event when logging it.

## CWE-783: Operator Precedence Logic Error

- URL: https://cwe.mitre.org/data/definitions/783.html
- Description: The product uses an expression in which operator precedence causes incorrect logic to be used.

## CWE-789: Memory Allocation with Excessive Size Value

- URL: https://cwe.mitre.org/data/definitions/789.html
- Description: The product allocates memory based on an untrusted, large size value, but it does not ensure that the size is within expected limits, allowing arbitrary amounts of memory to be allocated.

## CWE-798: Use of Hard-coded Credentials

- URL: https://cwe.mitre.org/data/definitions/798.html
- Description: The product contains hard-coded credentials, such as a password or cryptographic key.

## CWE-820: Missing Synchronization

- URL: https://cwe.mitre.org/data/definitions/820.html
- Description: The product utilizes a shared resource in a concurrent manner but does not attempt to synchronize access to the resource.

## CWE-821: Incorrect Synchronization

- URL: https://cwe.mitre.org/data/definitions/821.html
- Description: The product utilizes a shared resource in a concurrent manner, but it does not correctly synchronize access to the resource.

## CWE-835: Loop with Unreachable Exit Condition ('Infinite Loop')

- URL: https://cwe.mitre.org/data/definitions/835.html
- Description: The product contains an iteration or loop with an exit condition that cannot be reached, i.e., an infinite loop.

## CWE-611: Improper Restriction of XML External Entity Reference

- URL: https://cwe.mitre.org/data/definitions/611.html
- Description: The product processes an XML document that can contain XML entities with URIs that resolve to documents outside of the intended sphere of control, causing the product to embed incorrect documents into its output.

## CWE-1057: Data Access Operations Outside of Expected Data Manager Component

- URL: https://cwe.mitre.org/data/definitions/1057.html
- Description: The product uses a dedicated, central data manager component as required by design, but it contains code that performs data-access operations that do not use this data manager.

## CWE-415: Double Free

- URL: https://cwe.mitre.org/data/definitions/415.html
- Description: The product calls free() twice on the same memory address.

## CWE-416: Use After Free

- URL: https://cwe.mitre.org/data/definitions/416.html
- Description: The product reuses or references memory after it has been freed. At some point afterward, the memory may be allocated again and saved in another pointer, while the original pointer references a location somewhere within the new allocation. Any operations using the original pointer are no longer valid because the memory "belongs" to the code that operates on the new pointer.

