/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package tech.realcpf.imap.strong.kv.instance;

import org.apache.commons.lang3.ClassUtils;
import org.apache.seatunnel.engine.serializer.api.Serializer;
import org.apache.seatunnel.engine.serializer.protobuf.ProtoStuffSerializer;
import org.apache.seatunnel.engine.server.dag.physical.PipelineLocation;
import org.apache.seatunnel.engine.server.execution.TaskGroupLocation;
import tech.realcpf.imap.strong.kv.bean.KVObjectData;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface KVInstance {
    Serializer serializer = new ProtoStuffSerializer();
    Character KEY_FLAG = 'K';
    Character VALUE_FLAG = 'V';
    void build(Map<String,Object> prop);

    boolean set(Object key, Object value,String mapName);

    Object get(Object key,String mapName);

    boolean del(Object key, String mapName);

    List<Object> keys(String mapName);

    void shutdown();

    default KVObjectData encodeObject(Object value) {
        try {
            return KVObjectData.builder()
                    .className(ClassUtils.getName(value))
                    .data(serializer.serialize(value))
                    .build();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    default String formatKeyStr(Object o, String mapName,char flag) {
        if (o instanceof Long || o instanceof String) {
            return String.format("%s:%s:%s", mapName, flag,o);
        } else if (o instanceof PipelineLocation) {
            PipelineLocation p = (PipelineLocation) o;
            return String.format("%s:%s:%s-%s", mapName,flag, p.getJobId(), p.getPipelineId());
        } else if (o instanceof TaskGroupLocation) {
            TaskGroupLocation t = (TaskGroupLocation) o;
            return String.format("%s:%s:%s-%s-%s", mapName,flag, t.getJobId(), t.getTaskGroupId(), t.getPipelineId());
        } {
            return null;
        }
    }

}
