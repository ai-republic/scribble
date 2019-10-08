package sem.api;

import java.io.Serializable;

public interface ILinkFunction extends Serializable {
	void process(SemanticLink link);
}
