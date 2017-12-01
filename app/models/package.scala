import sangria.macros._

// TODO custom directives for post processing
// TODO (hardly necessary) Convert every field to a fragment (or avoid union error?)

package object models {

  val schema = graphql"""
  type Query {
    search(query: String!, limit: Int!): [Repository]!           @Proxy(route: "search(query:query,first:limit,type:REPOSITORY).nodes", fragment: true )
    repository(owner: String!, name: String!): Repository        @Proxy(route: "search(owner,name).nodes" )
  }

  type Repository {
    name: String!
    description: String!
    owner: RepositoryOwner!
    url: String!                      @Proxy(route: "homepageUrl" )
    tags: [String]!                   @Proxy(route: "repositoryTopics(first:4).nodes.topic.name" )
    stars: Connection                 @Proxy(route: "stargazers(last:2)" )
    forks: Connection                 @Proxy(route: "forks(last:2)" )
    commits: Connection               @Proxy(route: "commitComments(last:100)" )
    pullRequests: Connection          @Proxy(route: "pullRequests(last:100)" )
    issues: Connection                @Proxy(route: "issues(last:100)" )
    contributors: [RepositoryOwner]!  @Proxy(route: "mentionableUsers(first:100).nodes" )
  }

  type Connection {
    total: Int!                 @Proxy(route: "totalCount" )
    nodes: [Node]!
  }

  type Node {
    time: Float!                @Proxy(route: "createdAt" )
    author: RepositoryOwner!
  }

  type RepositoryOwner {
    username: String!           @Proxy(route: "login" )
    picture: String!            @Proxy(route: "avatarUrl" )
    url: String!
  }
  """

}
