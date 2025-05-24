
type integer = number

/**
 * @pattern ^[a-z0-9_.-]+:[a-z0-9/._-]+$
 */
type Identifier = string

/**
 * @pattern ^#[a-z0-9_.-]+:[a-z0-9/._-]+$
 */
type Tag = string

type SingleOrList<T> = T | T[]

interface ItemStack {
    id: Identifier,
    count?: integer,
    components?: {[key: string]: unknown},
}

interface Level {
     cost: integer,
     attributes: {[key: Identifier]: {
         amount: number,
         operation:
             | "add_value"
             | "add_multiplied_base"
             | "add_multiplied_total"
     }},
}

export interface Skill {
    icon: ItemStack,
    levels: (Level | integer)[],
    name?: string,
    description?: string,
}

interface LockList<T = SingleOrList<Identifier> | Tag> {
    entries: T,
    message?: string,
}

export interface LockGroup {
    requirements: SingleOrList<{[key: Identifier]: integer}>,
    item_name?: string,
    items?: LockList<SingleOrList<{ item: Identifier } | { tag: Identifier }>>,
    blocks?: LockList,
    entities?: LockList,
    recipes?: LockList<Identifier[]>,
}
