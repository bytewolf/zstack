package org.zstack.test.integration.kvm.vm

import org.zstack.sdk.ImageInventory
import org.zstack.sdk.InstanceOfferingInventory
import org.zstack.sdk.L2NetworkInventory
import org.zstack.sdk.L3NetworkInventory
import org.zstack.sdk.VmInstanceInventory
import org.zstack.test.integration.kvm.KvmTest
import org.zstack.testlib.EnvSpec
import org.zstack.testlib.SubCase
import org.zstack.utils.data.SizeUnit

class TestCreateVmInVariousNetworkTypesCase extends SubCase {
    EnvSpec env

    @Override
    void clean() {
        env.delete()
    }

    @Override
    void setup() {
        useSpring(KvmTest.springSpec)
    }

    @Override
    void environment() {
        env = env {
            instanceOffering {
                name = "instanceOffering"
                memory = SizeUnit.GIGABYTE.toByte(8)
                cpu = 4
            }

            diskOffering {
                name = "diskOffering"
                diskSize = SizeUnit.GIGABYTE.toByte(20)
            }

            sftpBackupStorage {
                name = "sftp"
                url = "/sftp"
                username = "root"
                password = "password"
                hostname = "localhost"

                image {
                    name = "image1"
                    url = "http://zstack.org/download/test.qcow2"
                }

                image {
                    name = "vr"
                    url = "http://zstack.org/download/vr.qcow2"
                }
            }

            zone {
                name = "zone"
                description = "test"

                cluster {
                    name = "cluster"
                    hypervisorType = "KVM"

                    kvm {
                        name = "kvm"
                        managementIp = "localhost"
                        username = "root"
                        password = "password"
                    }

                    attachPrimaryStorage("local")
                    attachL2Network("l2")
                }

                localPrimaryStorage {
                    name = "local"
                    url = "/local_ps"
                }

                l2NoVlanNetwork {
                    name = "l2"
                    physicalInterface = "eth0"

                    l3Network {
                        name = "l3"

                        ip {
                            startIp = "192.168.100.10"
                            endIp = "192.168.100.100"
                            netmask = "255.255.255.0"
                            gateway = "192.168.100.1"
                        }
                    }

                    l3Network {
                        name = "pubL3"
                        system =true

                        ip {
                            startIp = "12.16.10.10"
                            endIp = "12.16.10.100"
                            netmask = "255.255.255.0"
                            gateway = "12.16.10.1"
                        }
                    }
                }

                virtualRouterOffering {
                    name = "vr"
                    memory = SizeUnit.MEGABYTE.toByte(512)
                    cpu = 2
                    useManagementL3Network("pubL3")
                    usePublicL3Network("pubL3")
                    useImage("vr")
                }

                attachBackupStorage("sftp")
            }
        }
    }

    @Override
    void test() {
        env.create {
            createVmWithNetworkSystemIsTrue()
            createVmInNetworkWithOutService()
        }
    }

    void createVmWithNetworkSystemIsTrue() {
        InstanceOfferingInventory instanceOffering = env.inventoryByName("instanceOffering")
        ImageInventory image = env.inventoryByName("image1")
        L3NetworkInventory pubL3 = env.inventoryByName("pubL3")

        expect(AssertionError) {
            VmInstanceInventory vm = createVmInstance {
                name = "test"
                instanceOfferingUuid = instanceOffering.uuid
                imageUuid = image.uuid
                l3NetworkUuids = [pubL3.uuid]
            }
        }
    }

    void createVmInNetworkWithOutService() {
        InstanceOfferingInventory instanceOffering = env.inventoryByName("instanceOffering")
        ImageInventory image = env.inventoryByName("image1")
        L3NetworkInventory pubL3 = env.inventoryByName("pubL3")
        L2NetworkInventory l2 = env.inventoryByName("l2")

        L3NetworkInventory l3 = createL3Network {
            delegate.name = "testL3"
            delegate.l2NetworkUuid = l2.uuid
            delegate.systemTags = ["networkCategory::Private"]
        }
        assert l3.tags != null
        assert l3.tags.size() == 1
        assert l3.tags[0].tag == "networkCategory::Private"

        addIpRangeByNetworkCidr {
            delegate.name = "test-range"
            delegate.l3NetworkUuid = l3.uuid
            delegate.networkCidr = "192.168.101.0/24"
        }

        createVmInstance {
            name = "test"
            instanceOfferingUuid = instanceOffering.uuid
            imageUuid = image.uuid
            l3NetworkUuids = [l3.uuid]
        }

        expect(AssertionError) {
            createL3Network {
                delegate.name = "testL3"
                delegate.l2NetworkUuid = l2.uuid
                delegate.systemTags = ["networkCategory::Test"]
            }
        }

        expect(AssertionError) {
            createL3Network {
                delegate.name = "testL3"
                delegate.l2NetworkUuid = l2.uuid
                delegate.systemTags = ["networkCategory::private"]
            }
        }
    }
}
