package tech.realcpf.imap.strong.kv.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class KVObjectData implements Serializable {
    private byte[] data;

    private String className;
}
