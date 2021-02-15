package ie.ucd.dempsey.streamingsample.model;


import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public @Data
class Pair<F, S> {
    public F first;
    public S second;

    public Pair(F first, S second) {
        this.first = first;
        this.second = second;
    }
}
