package ie.ucd.dempsey.streamingsample.model;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
public @Data
class Pair<F, S> {
    public F first;
    public S second;
}
