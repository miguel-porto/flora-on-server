package pt.floraon.entities;

import java.io.IOException;

import com.arangodb.ArangoException;

public interface VertexWrapper {
	void saveToDB() throws IOException, ArangoException;
}
