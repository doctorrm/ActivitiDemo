1.activiti的版本是5的；
2.整合spring，暂时不考虑springboot（不稳定）；
3.流程图放在classpath的diagrams下面；
4.开始建表时用activiti.cfg.xml和右键项目run java application找到dbschema create来创建25张表，
  整合spring后activiti.cfg.xml基本可以忽略，而是依赖于spring-**配置来实现所有的配置问题；
5. 