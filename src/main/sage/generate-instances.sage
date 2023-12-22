def exportBipartiteToFile(G, filename, k1, k2):
    vG = G.vertices(sort=True)
    v1 = [v for v in vG if v[0] == k1 ]
    v2 = [v for v in vG if v[0] == k2 ]
    i = 1
    vmap = {}
    for v in v1:
        vmap[v]=i
        i=i+1
    for v in v2:
        vmap[v]=i
        i=i+1

    with open(filename, 'w') as file:
        print("p ocm {} {} {}".format(len(v1), len(v2), len(G)), file = file)
        for edge in G.edges(sort = True):
            print("{} {}".format(vmap[edge[0]], vmap[edge[1]]), file = file)



def generateRandomBicubicPlanar(orders, number):
    for n in orders:
        for i in range(1, number + 1):
            G = graphs.RandomBicubicPlanar(n)
            G.allow_multiple_edges(False)
            filename="CN{:0>3}-{}.gr".format(n, i)
            exportBipartiteToFile(G, filename, 'i', 'n')

def generateRandomBipartite(orders, densities, number):
    for n in orders:
        for d in densities:
            for i in range(1, number + 1):
                G = graphs.RandomBipartite(n // 2, n - n // 2, d / 1000)
                G.allow_multiple_edges(False)
                filename="N{:0>3}D{:0>3}-{}.gr".format(n, d, i)
                exportBipartiteToFile(G, filename, 0, 1)



m = 2
orders = [10, 25, 50, 100]
densities = [10, 50, 100, 200]

generateRandomBicubicPlanar(orders, m)
generateRandomBipartite(orders, densities, m)
