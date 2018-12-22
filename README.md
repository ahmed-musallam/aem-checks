# AEM Checks

**[This is still a WIP]**

Often times, developers make assumptions about the state of an AEM instance, assumptions such as:

- If nodes exist, have certain properties 
- If OSGI components/services are in a certain state or bind a certain service implementation
- If certain users/groups exist and have access (specific privileges) to certain paths

## Goal
The goal of this project is to provide a way to manage AEM checks such as above via an AEM admin UI  and also via deployable configuration (from source code). Through the UI, any user can run AEM checks and check their status and history.

> The main thing, really, is to make checks configuration-based instead of code-based, while providing an API and utilities to support custom checks.

## Why not sling health checks?

well, several reasons:
- The UI/UX (felix web console) is terrible.
- The API is not straight-forward for returning error status and does not provide history data.

## Checks

### Node Checks
- Node exists
- Node has Property

### OSGI Checks

- OSGI component exists and is in a specific state (TODO- add more detail on states)
- OSGI component has satisfied reference to a certain component implementation.

### Authorizable Checks (users and groups)
- An Authorizable exists
- An Authorizable exists and has certain privileges in certain paths
- An Authorizable belongs to a specific group.


## Implementation

### Configuration Storage

Checks configuration will be stored in the JCR as nodes that use custom node types for each type of check. See "Node Types" below. The configuration is then picked up and processed to run the check.


## Node Types

### Base

This is the base mixin that contains the title and description for a check/suite.

```
[ch:Base]
  mixin
  - ch:Title (string) mandatory
  - ch:Description (string) mandatory
```


### Suite
A suite is the parent for a collection of simple checks. This node type contains contains the title/description and a node named 

```
[ch:Suite] > ch:Base
  + ch:CheckCollection 
    mandatory autocreated protected
```

### Node Checks

#### Base node check mixin

```
[ch:BaseNode]
   mixin
	- ch:Path (string) mandatory
```

#### Node exists check

```
[ch:NodeExists] > ch:BaseNode, ch:Base
  - ch:NodePath (string) mandatory
  - ch:Negate (boolean)
```

The `ch:Negate` checks for the negate/inverse of the test result.

#### Properties exists check

```
[ch:PropertiesExist] > ch:BaseNode, ch:Base, ch:NodeExists
  - ch:properties (string) mandatory multiple
```

`ch:properties` is a multi-value property, each property can be defined with the following BNF

```
<property-value> ::= <property-name> | <property-name> '=' <property-value>
```

the following values are valid:

- `myProperty`: will check if `myProperty` property exists
- `myProperty=someValue`: will check if `myProperty` property exists and it's value matches `someValue `



### OSGI Checks

#### OSGI Component Base Check Mixin

```
[ch:OSGIComponentBase]
   mixin
	- ch:PID (string) mandatory
	- ch:Negate (boolean)
```

#### OSGI Component State check 

```
[ch:OSGIComponentState] > ch:OSGIComponentBase, ch:Base
	- ch:State (string) mandatory
```

`ch:State` can be any of the following:  `UNSATISFIED_CONFIGURATION`, `UNSATISFIED_REFERENCE`, `SATISFIED` or `ACTIVE`.

 see [the OSGI api](https://osgi.org/javadoc/r6/cmpn/index.html?org/osgi/service/component/runtime/dto/ComponentConfigurationDTO.html) for information on each state


#### OSGI Component has satisfied reference
 
```
[ch:OSGIComponentReferences] > ch:OSGIComponentBase, ch:Base
	- ch:References (string) mandatory multiple
```

`ch:References` is a string multi-value property where each value is a component PID OR **implementation** class full qualified name. This so that we can check if an exact implementation is referenced and satisfied (exists).

### Authorizable Checks

#### Authorizable Base Check

```
[ch:AuthorizableBase]
   mixin
	- ch:Authorizables (string) mandatory multiple
```

#### Authorizable/s Exists Check

```
[ch:AuthorizableExists] > ch:AuthorizableBase, ch:Base
```

#### Authorizable/s Has Privileges to path/s Check

// TODO - enhance to be certain path with certain privileges. Possible as child nodes.

```
[ch:AuthorizableHasPrivileges] > ch:AuthorizableBase, ch:Base
	- ch:Paths (string) mandatory multiple
	- ch:Privileges (string) mandatory multiple
```

#### Authorizable/s belongs to specific group/s

```
[ch:AuthorizableBelongs] > ch:AuthorizableBase
	- ch:Groups (string) mandatory multiple
```


## Example check stored in JCR

```
+ my-check > ch:Suite
  - ch:Title = "My Company Code and Content exists"
  - ch:Description = "If this check fails, this is catastrophic"
  + checks > ch:CheckCollection 

  	+ check1 > ch:NodeExists
  	  - ch:Title = "My Company Content exists"
     - ch:Description = "Checks if /content/my-company exists"
  	  - ch:NodePath = "/content/my-company"

  	+ check2 > ch:NodeExists
  	  - ch:Title = "My Company Code exists"
     - ch:Description = "Checks if /apps/my-company exists"
  	  - ch:NodePath = "/apps/my-company"
```
