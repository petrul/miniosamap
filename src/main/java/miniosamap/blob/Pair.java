package miniosamap.blob;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @AllArgsConstructor @NoArgsConstructor
public class Pair<T, R> {

    protected T first;
    protected R second;

}
