#
# This file is part of lignes-claires, https://github.com/arnaud-m/lignes-claires
#
# Copyright (c) 2024, Université Côte d'Azur. All rights reserved.
#
# Licensed under the BSD 3-clause license.
# See LICENSE file in the project root for full license information.
#

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
        print("p ocm {} {} {}".format(len(v1), len(v2), G.size()), file = file)
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
                G = graphs.RandomBipartite(n // 2, n - n // 2, d)
                G.allow_multiple_edges(False)
                filename="N{:0>3}D{:0>5}-{}.gr".format(n, int(d * 10^5) , i)
                exportBipartiteToFile(G, filename, 0, 1)



m = 5
orders = [50, 75, 100, 150, 200]

generateRandomBicubicPlanar(orders, m)

densities = [1, 2, 3]
for n in orders:
    generateRandomBipartite(orders, [d / n for d in densities], m)
