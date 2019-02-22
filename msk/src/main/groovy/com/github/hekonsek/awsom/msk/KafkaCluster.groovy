package com.github.hekonsek.awsom.msk

import com.amazonaws.services.kafka.AWSKafkaClientBuilder
import com.amazonaws.services.kafka.model.BrokerNodeGroupInfo
import com.amazonaws.services.kafka.model.CreateClusterRequest

class KafkaCluster {

    private String name

    private String kafkaVersion = '2.1.0'

    static KafkaCluster kafkaCluster() {
        new KafkaCluster()
    }

    void create() {
        if (name == null) {
            throw new RuntimeException()
        }

        def createClusterConfig = new CreateClusterRequest()
        createClusterConfig.setClusterName(name)
        createClusterConfig.kafkaVersion = kafkaVersion
        createClusterConfig.numberOfBrokerNodes = 3
        def brokerNodesConfig = new BrokerNodeGroupInfo()
        brokerNodesConfig.instanceType = 'kafka.m5.large'
        brokerNodesConfig.clientSubnets = Arrays.asList("subnet-0d13f4a342a19c046", "subnet-05f64e2b6e49a3e58", "subnet-0e9cab3338abca006")
        createClusterConfig.brokerNodeGroupInfo = brokerNodesConfig
        AWSKafkaClientBuilder.standard().build().createCluster(createClusterConfig)
    }

    String name() {
        return name
    }

    KafkaCluster name(String name) {
        this.name = name
        this
    }

    String kafkaVersion() {
        return kafkaVersion
    }

    KafkaCluster kafkaVersion(String kafkaVersion) {
        this.kafkaVersion = kafkaVersion
        this
    }

    static void main(String... args) {
        kafkaCluster().
                name('xxx').
                create()
    }

}